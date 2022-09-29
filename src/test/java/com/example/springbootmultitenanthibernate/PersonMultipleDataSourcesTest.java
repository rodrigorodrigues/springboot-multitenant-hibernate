package com.example.springbootmultitenanthibernate;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.example.springbootmultitenanthibernate.PersonControllerTest.X_DATASOURCE_ID;
import static com.example.springbootmultitenanthibernate.PersonControllerTest.X_TENANT_ID;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@SpringBootTest(properties = {
        "logging.level.com.example.springbootmultitenanthibernate=trace",
//        "logging.level.org.springframework=trace"
})
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("multiple-datasources")
@ContextConfiguration(initializers = Application.MultipleDataSourcesInitializer.class)
@Slf4j
public class PersonMultipleDataSourcesTest {
    @Autowired
    MockMvc mockMvc;

    /*@Container
    public static OracleContainer oracleDB = new OracleContainer("gvenzl/oracle-xe:18.4.0-slim")
            .withDatabaseName("test")
            .withInitScript("schema-oracle.sql");


    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        log.info("oracle connection: {}", oracleDB.getJdbcUrl()+"="+oracleDB.getUsername()+"="+oracleDB.getPassword());
        registry.add("datasource.multiple.map['ORACLE'].url",oracleDB::getJdbcUrl);
        registry.add("datasource.multiple.map['ORACLE'].username", oracleDB::getUsername);
        registry.add("datasource.multiple.map['ORACLE'].password", oracleDB::getPassword);
    }*/

    /*@Container
    public static PostgreSQLContainer<?> postgresDB = new PostgreSQLContainer<>
            ("postgres:13.2")
            .withDatabaseName("generic")
            .withUsername("postgres")
            .withPassword("postgres")
            .withInitScript("schema-h2.sql")
            .withPrivilegedMode(true);


    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",postgresDB::getJdbcUrl);
        registry.add("spring.datasource.username", postgresDB::getUsername);
        registry.add("spring.datasource.password", postgresDB::getPassword);
    }*/

    @Test
//    @Sql("/data-mysql.sql")
    void testGenericSchema() throws Exception {
        mockMvc.perform(get("/person").header(X_TENANT_ID, "CustomerA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*]", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Test Customer A - Oracle"));

        mockMvc.perform(get("/person").header(X_TENANT_ID, "CustomerA").header(X_DATASOURCE_ID, "POSTGRESQL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*]", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Test Customer A - Postgresql"));

        mockMvc.perform(get("/person").header(X_TENANT_ID, "CustomerB").header(X_DATASOURCE_ID, "MYSQL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*]", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Test Customer B - Mysql"));

        mockMvc.perform(get("/person").header(X_TENANT_ID, "CustomerB").header(X_DATASOURCE_ID, "H2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*]", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.content[0].name").isNotEmpty());
    }
}
