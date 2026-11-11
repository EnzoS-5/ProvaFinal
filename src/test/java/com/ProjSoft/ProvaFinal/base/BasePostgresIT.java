package com.ProjSoft.ProvaFinal.base;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_TESTS", matches = "true")
class BasePostgresIT extends BaseControllerIntegrationTest {
  @DynamicPropertySource
  static void database(DynamicPropertyRegistry properties) {
    properties.add("spring.datasource.url", () -> System.getenv("IT_DB_URL"));
    properties.add("spring.datasource.username", () -> System.getenv("IT_DB_USER"));
    properties.add("spring.datasource.password", () -> System.getenv("IT_DB_PASSWORD"));
    properties.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
  }
}
