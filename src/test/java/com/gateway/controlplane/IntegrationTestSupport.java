package com.gateway.controlplane;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "app.seed-demo-data=false"
})
abstract class IntegrationTestSupport {
}
