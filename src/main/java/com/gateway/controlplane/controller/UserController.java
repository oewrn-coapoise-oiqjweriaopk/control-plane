package com.gateway.controlplane.controller;

import com.gateway.controlplane.entity.AdminUser;
import com.gateway.controlplane.service.ReferenceDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Read platform users and RBAC assignments.")
public class UserController {

    private final ReferenceDataService referenceDataService;

    public UserController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping
    @Operation(summary = "List admin users", description = "Returns platform users and their RBAC metadata.")
    public List<AdminUser> list() {
        return referenceDataService.findUsers();
    }
}
