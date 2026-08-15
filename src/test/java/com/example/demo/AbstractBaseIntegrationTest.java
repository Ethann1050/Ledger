package com.example.demo;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
public abstract class AbstractBaseIntegrationTest {

    // 1. Declare a single static container instance
    protected static final PostgreSQLContainer<?> POSTGRES;

    static {
        // 2. Configure the container
        POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true); // Tells Testcontainers to reuse a running container if matched

        // 3. Start the container in a static block
        POSTGRES.start();
    }

    // 4. Inject dynamic connection values into Spring's Environment
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }
}