//! Enhanced Rules System for API Gateway
//! 
//! Features:
//! - Shopping cart-style rule addition (add one by one)
//! - Sequential rule evaluation
//! - Consume rule for parameter stripping
//! - Redis-backed persistence with organized key-value structure

use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use std::collections::HashMap;

// ============================================================================
// CORE RULE TYPES
// ============================================================================

/// Individual rule that can be applied to a request
#[derive(Deserialize, Serialize, Debug, Clone)]
#[serde(tag = "type", content = "config")]
pub enum Rule {
    /// Allow requests to specific path pattern
    #[serde(rename = "allow_path")]
    AllowPath { path: String },

    /// Deny requests to specific path pattern
    #[serde(rename = "deny_path")]
    DenyPath { path: String },

    /// Allow specific HTTP methods
    #[serde(rename = "allow_method")]
    AllowMethod { methods: Vec<HttpMethod> },

    /// Deny specific HTTP methods
    #[serde(rename = "deny_method")]
    DenyMethod { methods: Vec<HttpMethod> },

    /// Route requests to upstream service
    #[serde(rename = "route")]
    Route { upstream: String },

    /// Rate limit: requests per time frame
    #[serde(rename = "rate_limit")]
    RateLimit {
        limit: u32,
        /// Time frame in seconds
        frame_seconds: usize,
    },

    /// Require header to be present
    #[serde(rename = "require_header")]
    RequireHeader { key: String },

    /// Check header value matches
    #[serde(rename = "header_equals")]
    HeaderEquals { key: String, value: String },

    /// Request timeout in milliseconds
    #[serde(rename = "timeout")]
    Timeout { millis: u64 },

    /// CONSUME RULE (Special): Remove parameters from request before forwarding
    /// After validation passes, strips specified parameters and remakes request
    /// Only forwarded to upstream microservice (not to client)
    #[serde(rename = "consume")]
    Consume {
        /// Parameters to remove from request
        parameters: Vec<String>,
        /// Optional: whether to strip from query, body, or both
        strip_from: Option<ConsumeSource>,
    },

    /// Custom authentication validation
    #[serde(rename = "auth")]
    Auth {
        /// Type of auth: "bearer", "api_key", "basic"
        scheme: AuthScheme,
        /// Required scopes
        required_scopes: Option<Vec<String>>,
    },

    /// Request body size limit in bytes
    #[serde(rename = "body_size_limit")]
    BodySizeLimit { max_bytes: usize },

    /// IP whitelist/blacklist
    #[serde(rename = "ip_filter")]
    IpFilter {
        /// Whitelist IPs (if present, deny all others)
        whitelist: Option<Vec<String>>,
        /// Blacklist IPs (deny these)
        blacklist: Option<Vec<String>>,
    },

    /// Transform request/response
    #[serde(rename = "transform")]
    Transform {
        /// Action: "add_header", "remove_header", "rewrite_path"
        action: String,
        /// Target (header name, path pattern, etc)
        target: String,
        /// Value (header value, replacement path, etc)
        value: Option<String>,
    },
}

/// HTTP method enumeration
#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
pub enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    HEAD,
    OPTIONS,
}

/// Source for consume rule parameter stripping
#[derive(Deserialize, Serialize, Debug, Clone)]
#[serde(rename_all = "lowercase")]
pub enum ConsumeSource {
    Query,  // Strip from query string
    Body,   // Strip from request body
    Both,   // Strip from both query and body
}

/// Authentication scheme types
#[derive(Deserialize, Serialize, Debug, Clone)]
#[serde(rename_all = "snake_case")]
pub enum AuthScheme {
    Bearer,
    ApiKey,
    Basic,
}

// ============================================================================
// POLICY: A SET OF RULES (SEQUENTIAL)
// ============================================================================

/// A policy is a named collection of rules evaluated sequentially
#[derive(Deserialize, Serialize, Debug, Clone)]
pub struct Policy {
    /// Unique policy identifier
    pub id: i64,

    /// Human-readable name
    pub name: String,

    /// Optional description
    pub description: Option<String>,

    /// Sequential list of rules (evaluated in order)
    pub rules: Vec<Rule>,

    /// Whether this policy is active
    pub enabled: bool,

    /// Routes this policy applies to (patterns like "/api/users/:id")
    pub apply_to_routes: Vec<String>,

    /// API keys this policy applies to (if empty, applies to all)
    pub apply_to_keys: Vec<String>,

    /// Priority (higher = evaluated first)
    pub priority: i32,

    /// When this policy was created
    pub created_at: DateTime<Utc>,

    /// When this policy was last updated
    pub updated_at: DateTime<Utc>,
}

impl Policy {
    /// Create a new empty policy (shopping cart mode)
    pub fn new(id: i64, name: String) -> Self {
        let now = Utc::now();
        Self {
            id,
            name,
            description: None,
            rules: Vec::new(),
            enabled: true,
            apply_to_routes: Vec::new(),
            apply_to_keys: Vec::new(),
            priority: 0,
            created_at: now,
            updated_at: now,
        }
    }

    /// Add a rule to this policy (shopping cart item)
    pub fn add_rule(&mut self, rule: Rule) {
        self.rules.push(rule);
        self.updated_at = Utc::now();
    }

    /// Remove a rule by index
    pub fn remove_rule(&mut self, index: usize) -> Option<Rule> {
        if index < self.rules.len() {
            self.updated_at = Utc::now();
            Some(self.rules.remove(index))
        } else {
            None
        }
    }

    /// Get the list of "consume" rules for parameter stripping
    pub fn consume_rules(&self) -> Vec<&Rule> {
        self.rules
            .iter()
            .filter(|r| matches!(r, Rule::Consume { .. }))
            .collect()
    }

    /// Check if policy applies to a given route
    pub fn applies_to_route(&self, route: &str) -> bool {
        if self.apply_to_routes.is_empty() {
            return true; // Empty means applies to all routes
        }
        self.apply_to_routes.iter().any(|pattern| {
            // Simple pattern matching: exact or wildcard
            pattern == route || pattern.ends_with('*') && route.starts_with(&pattern[..pattern.len() - 1])
        })
    }

    /// Check if policy applies to a given API key
    pub fn applies_to_key(&self, key_id: i64) -> bool {
        if self.apply_to_keys.is_empty() {
            return true; // Empty means applies to all keys
        }
        self.apply_to_keys.iter().any(|k| k.parse::<i64>().ok() == Some(key_id))
    }
}

// ============================================================================
// POLICY SET: ALL POLICIES IN CART (UNSAVED)
// ============================================================================

/// Shopping cart style policy builder (before persistence)
#[derive(Deserialize, Serialize, Debug, Clone, Default)]
pub struct PolicyCart {
    /// Policy being built
    pub policy: Option<Policy>,

    /// Rules added so far (in order)
    pub rules: Vec<Rule>,

    /// Metadata
    pub routes: Vec<String>,
    pub keys: Vec<String>,
    pub priority: i32,
}

impl PolicyCart {
    /// Start a new policy in the cart
    pub fn new_policy(id: i64, name: String) -> Self {
        Self {
            policy: Some(Policy::new(id, name)),
            rules: Vec::new(),
            routes: Vec::new(),
            keys: Vec::new(),
            priority: 0,
        }
    }

    /// Add a rule to cart
    pub fn add_rule(&mut self, rule: Rule) -> Result<(), String> {
        if self.policy.is_none() {
            return Err("No policy in cart. Start with new_policy()".to_string());
        }
        self.rules.push(rule);
        Ok(())
    }

    /// Remove last rule (undo)
    pub fn remove_last_rule(&mut self) -> Option<Rule> {
        self.rules.pop()
    }

    /// View current cart contents
    pub fn preview(&self) -> CartPreview {
        CartPreview {
            rules_count: self.rules.len(),
            routes: self.routes.clone(),
            keys: self.keys.clone(),
            priority: self.priority,
            rules: self.rules.clone(),
        }
    }

    /// Finalize and save policy (commit cart)
    pub fn commit(mut self) -> Result<Policy, String> {
        let mut policy = self.policy.ok_or("No policy in cart")?;

        // Add all rules
        policy.rules = self.rules;

        // Set apply_to routes and keys
        policy.apply_to_routes = self.routes;
        policy.apply_to_keys = self.keys;
        policy.priority = self.priority;

        Ok(policy)
    }
}

/// Preview of cart contents before commit
#[derive(Serialize, Debug)]
pub struct CartPreview {
    pub rules_count: usize,
    pub routes: Vec<String>,
    pub keys: Vec<String>,
    pub priority: i32,
    pub rules: Vec<Rule>,
}

// ============================================================================
// REDIS STORAGE STRUCTURE
// ============================================================================

/// How data is stored in Redis (organized key-value structure)
/// 
/// ```
/// Key Structure:
/// ├── gateway:policies:           (Sorted set with policy IDs by priority)
/// ├── gateway:policy:{id}:meta    (Policy metadata)
/// ├── gateway:policy:{id}:rules   (Rules JSON array)
/// │
/// ├── gateway:routes:             (All routes)
/// ├── gateway:route:{id}:         (Individual route)
/// │
/// ├── gateway:keys:               (All API keys)
/// ├── gateway:key:{id}:           (Individual key)
/// │
/// └── gateway:policy_carts:{user} (User's shopping cart)
/// ```
#[derive(Debug)]
pub struct RedisStructure;

impl RedisStructure {
    /// Get key for all policies (sorted set by priority)
    pub fn policies_key() -> &'static str {
        "gateway:policies"
    }

    /// Get key for individual policy metadata
    pub fn policy_meta_key(policy_id: i64) -> String {
        format!("gateway:policy:{}:meta", policy_id)
    }

    /// Get key for individual policy rules
    pub fn policy_rules_key(policy_id: i64) -> String {
        format!("gateway:policy:{}:rules", policy_id)
    }

    /// Get key for all routes
    pub fn routes_key() -> &'static str {
        "gateway:routes"
    }

    /// Get key for individual route
    pub fn route_key(route_id: i64) -> String {
        format!("gateway:route:{}", route_id)
    }

    /// Get key for all API keys
    pub fn keys_key() -> &'static str {
        "gateway:keys"
    }

    /// Get key for individual API key
    pub fn key_key(key_id: i64) -> String {
        format!("gateway:key:{}", key_id)
    }

    /// Get key for user's shopping cart
    pub fn cart_key(user_id: &str) -> String {
        format!("gateway:policy_carts:{}", user_id)
    }
}

// ============================================================================
// RULE EVALUATION & CONSUME RULE HANDLING
// ============================================================================

/// Result of evaluating a rule
#[derive(Debug, Clone)]
pub enum RuleEvaluation {
    /// Allow the request to continue
    Allow,
    /// Deny the request
    Deny(String), // reason
    /// Modify the request (for consume rules, header rewrites, etc)
    Transform(RequestTransform),
}

/// Request transformation (for consume rules)
#[derive(Debug, Clone)]
pub struct RequestTransform {
    /// Parameters to strip from query
    pub strip_query: Vec<String>,
    /// Parameters to strip from body
    pub strip_body: Vec<String>,
    /// Headers to add
    pub add_headers: HashMap<String, String>,
    /// Headers to remove
    pub remove_headers: Vec<String>,
    /// Path rewrite (optional)
    pub rewrite_path: Option<String>,
}

impl Rule {
    /// Evaluate this rule against a request context
    /// Used for authorization/validation before forwarding
    pub fn evaluate(&self, context: &RuleContext) -> RuleEvaluation {
        match self {
            Rule::AllowPath { path } => {
                if path_matches(path, &context.request_path) {
                    RuleEvaluation::Allow
                } else {
                    RuleEvaluation::Deny(format!("Path {} not allowed", context.request_path))
                }
            }

            Rule::DenyPath { path } => {
                if path_matches(path, &context.request_path) {
                    RuleEvaluation::Deny(format!("Path {} is denied", context.request_path))
                } else {
                    RuleEvaluation::Allow
                }
            }

            Rule::AllowMethod { methods } => {
                if methods.contains(&context.method) {
                    RuleEvaluation::Allow
                } else {
                    RuleEvaluation::Deny(format!(
                        "Method {:?} not allowed. Allowed: {:?}",
                        context.method, methods
                    ))
                }
            }

            Rule::DenyMethod { methods } => {
                if methods.contains(&context.method) {
                    RuleEvaluation::Deny(format!("Method {:?} is denied", context.method))
                } else {
                    RuleEvaluation::Allow
                }
            }

            Rule::RateLimit { limit, frame_seconds } => {
                // Rate limit checking would be done by data-plane
                // This is a marker rule for the rate limiter to use
                RuleEvaluation::Allow // Actual limiting happens in gateway
            }

            Rule::RequireHeader { key } => {
                if context.headers.contains_key(key) {
                    RuleEvaluation::Allow
                } else {
                    RuleEvaluation::Deny(format!("Required header '{}' missing", key))
                }
            }

            Rule::HeaderEquals { key, value } => {
                match context.headers.get(key) {
                    Some(header_value) if header_value == value => RuleEvaluation::Allow,
                    Some(header_value) => {
                        RuleEvaluation::Deny(format!(
                            "Header '{}' = '{}', expected '{}'",
                            key, header_value, value
                        ))
                    }
                    None => RuleEvaluation::Deny(format!("Header '{}' not found", key)),
                }
            }

            Rule::Consume {
                parameters,
                strip_from,
            } => {
                // Consume rule: strip parameters after validation
                let mut transform = RequestTransform {
                    strip_query: Vec::new(),
                    strip_body: Vec::new(),
                    add_headers: HashMap::new(),
                    remove_headers: Vec::new(),
                    rewrite_path: None,
                };

                match strip_from {
                    Some(ConsumeSource::Query) => {
                        transform.strip_query = parameters.clone();
                    }
                    Some(ConsumeSource::Body) => {
                        transform.strip_body = parameters.clone();
                    }
                    Some(ConsumeSource::Both) | None => {
                        transform.strip_query = parameters.clone();
                        transform.strip_body = parameters.clone();
                    }
                }

                RuleEvaluation::Transform(transform)
            }

            Rule::Auth {
                scheme,
                required_scopes,
            } => {
                // Auth validation
                match scheme {
                    AuthScheme::Bearer => {
                        if context.headers.contains_key("authorization") {
                            RuleEvaluation::Allow
                        } else {
                            RuleEvaluation::Deny("Bearer token required".to_string())
                        }
                    }
                    AuthScheme::ApiKey => {
                        if context.headers.contains_key("x-api-key")
                            || context.headers.contains_key("authorization")
                        {
                            RuleEvaluation::Allow
                        } else {
                            RuleEvaluation::Deny("API key required".to_string())
                        }
                    }
                    AuthScheme::Basic => {
                        if context.headers.contains_key("authorization") {
                            RuleEvaluation::Allow
                        } else {
                            RuleEvaluation::Deny("Basic authentication required".to_string())
                        }
                    }
                }
            }

            Rule::Route { .. } => RuleEvaluation::Allow, // Routing happens in gateway

            Rule::Timeout { .. } => RuleEvaluation::Allow, // Timeout is set in gateway

            Rule::BodySizeLimit { max_bytes } => {
                if let Some(size) = context.body_size {
                    if size > *max_bytes {
                        RuleEvaluation::Deny(format!(
                            "Body size {} exceeds limit {}",
                            size, max_bytes
                        ))
                    } else {
                        RuleEvaluation::Allow
                    }
                } else {
                    RuleEvaluation::Allow
                }
            }

            Rule::IpFilter {
                whitelist,
                blacklist,
            } => {
                if let Some(whitelist) = whitelist {
                    if whitelist.contains(&context.client_ip) {
                        RuleEvaluation::Allow
                    } else {
                        RuleEvaluation::Deny("IP not whitelisted".to_string())
                    }
                } else if let Some(blacklist) = blacklist {
                    if blacklist.contains(&context.client_ip) {
                        RuleEvaluation::Deny("IP is blacklisted".to_string())
                    } else {
                        RuleEvaluation::Allow
                    }
                } else {
                    RuleEvaluation::Allow
                }
            }

            Rule::Transform { .. } => RuleEvaluation::Allow, // Transform is applied separately

            _ => RuleEvaluation::Allow,
        }
    }
}

// ============================================================================
// RULE EVALUATION CONTEXT
// ============================================================================

/// Context for rule evaluation
#[derive(Debug, Clone)]
pub struct RuleContext {
    pub request_path: String,
    pub method: HttpMethod,
    pub headers: HashMap<String, String>,
    pub client_ip: String,
    pub body_size: Option<usize>,
}

// ============================================================================
// HELPER FUNCTIONS
// ============================================================================

fn path_matches(pattern: &str, path: &str) -> bool {
    if pattern == "*" || pattern == "/*" {
        return true;
    }

    let pattern_segments: Vec<&str> = pattern
        .trim_matches('/')
        .split('/')
        .filter(|s| !s.is_empty())
        .collect();
    let path_segments: Vec<&str> = path
        .trim_matches('/')
        .split('/')
        .filter(|s| !s.is_empty())
        .collect();

    if pattern_segments.len() != path_segments.len() {
        return false;
    }

    pattern_segments
        .iter()
        .zip(path_segments.iter())
        .all(|(p, path)| p.starts_with(':') || p == path)
}

// ============================================================================
// TESTS
// ============================================================================

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn policy_cart_workflow() {
        let mut cart = PolicyCart::new_policy(1, "user_policy".to_string());

        // Add rules one by one
        cart.add_rule(Rule::AllowMethod {
            methods: vec![HttpMethod::GET, HttpMethod::POST],
        })
        .unwrap();

        cart.add_rule(Rule::RateLimit {
            limit: 100,
            frame_seconds: 60,
        })
        .unwrap();

        cart.add_rule(Rule::RequireHeader {
            key: "authorization".to_string(),
        })
        .unwrap();

        // Preview cart
        let preview = cart.preview();
        assert_eq!(preview.rules_count, 3);

        // Commit policy
        let policy = cart.commit().unwrap();
        assert_eq!(policy.rules.len(), 3);
    }

    #[test]
    fn consume_rule_strips_parameters() {
        let rule = Rule::Consume {
            parameters: vec!["api_key".to_string(), "secret".to_string()],
            strip_from: Some(ConsumeSource::Query),
        };

        let context = RuleContext {
            request_path: "/api/data".to_string(),
            method: HttpMethod::GET,
            headers: HashMap::new(),
            client_ip: "127.0.0.1".to_string(),
            body_size: None,
        };

        match rule.evaluate(&context) {
            RuleEvaluation::Transform(transform) => {
                assert_eq!(transform.strip_query, vec!["api_key", "secret"]);
            }
            _ => panic!("Expected Transform"),
        }
    }

    #[test]
    fn policy_applies_to_route() {
        let mut policy = Policy::new(1, "test".to_string());
        policy.apply_to_routes = vec!["/api/users/:id".to_string(), "/api/posts/*".to_string()];

        assert!(policy.applies_to_route("/api/users/42"));
        assert!(policy.applies_to_route("/api/posts/123"));
        assert!(!policy.applies_to_route("/api/comments/1"));
    }

    #[test]
    fn redis_key_structure() {
        assert_eq!(RedisStructure::policies_key(), "gateway:policies");
        assert_eq!(RedisStructure::policy_meta_key(1), "gateway:policy:1:meta");
        assert_eq!(
            RedisStructure::policy_rules_key(1),
            "gateway:policy:1:rules"
        );
        assert_eq!(RedisStructure::cart_key("user123"), "gateway:policy_carts:user123");
    }
}
