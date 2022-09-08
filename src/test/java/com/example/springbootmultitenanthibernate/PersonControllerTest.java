package com.example.springbootmultitenanthibernate;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.AssertionErrors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@AutoConfigureMockMvc
class PersonControllerTest {
    public static final String X_TENANT_ID = "X-TenantID";
    @Autowired
    MockMvc mockMvc;

    @Autowired
    PersonRepository personRepository;

    @AfterEach
    void tearDown() {
        personRepository.deleteAllRegardlessOfTenant();
    }

    @Test
    @Order(1)
    void testGenericSchema() throws Exception {
        mockMvc.perform(get("/person").header(X_TENANT_ID, "CustomerA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*]", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Test Customer A"));

        mockMvc.perform(get("/person").header(X_TENANT_ID, "CustomerB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Customer B"));

        mockMvc.perform(get("/person").header(X_TENANT_ID, "CustomerC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*]").isEmpty());

        String payload = """
                {
                "name": "Test Customer C"
                }
                """;

        mockMvc.perform(post("/person").header(X_TENANT_ID, "CustomerC")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        mockMvc.perform(get("/person").header(X_TENANT_ID, "CustomerC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Customer C"));
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(ints = {4, 8, 12, 16, 32, 50, 77, 100})
    void serialAccessToPersonByDifferentTenants(int iterations) throws Exception {
        ResultActions resultActions;
        for (int i = 0; i < iterations; i++) {
            String name = UUID.randomUUID().toString();
            String payload = String.format("""
                                           {
                                           "name": "name-%s"
                                           }
                                           """,
                                           name);
            resultActions = mockMvc.perform(post("/person").header(X_TENANT_ID, "vmware")
                                                           .contentType(MediaType.APPLICATION_JSON)
                                                           .content(payload));
            resultActions.andExpect(status().isOk());

            resultActions = mockMvc.perform(get("/person")
                    .header(X_TENANT_ID, "vmware")
                    .queryParam("name", "name-"+name));
            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("name-"+name)))
                    .andDo(print());
        }
        resultActions = mockMvc.perform(get("/person")
                .header(X_TENANT_ID, "vmware")
                .queryParam("size", ""+iterations));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name", hasSize(iterations)))
                .andDo(print());
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(ints = {4, 8, 12, 16, 32, 50, 77, 100})
    void concurrentAccessToPersonByDifferentTenants(int threadCount) {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicBoolean failed = new AtomicBoolean(false);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                awaitOnLatch(startLatch);

                String tenant = UUID.randomUUID().toString();
                ResultActions resultActions;
                try {
                    String payload = String.format("""
                                                   {
                                                   "name": "name-%s"
                                                   }
                                                   """,
                                                   tenant);
                    resultActions = mockMvc.perform(post("/person").header(X_TENANT_ID, tenant)
                                                                   .contentType(MediaType.APPLICATION_JSON)
                                                                   .content(payload));
                    resultActions.andExpect(status().isOk());

                    resultActions = mockMvc.perform(get("/person").header(X_TENANT_ID, tenant));
                    resultActions.andExpect(status().isOk())
                                 .andExpect(jsonPath("$.content.[*].name", containsInAnyOrder("name-" + tenant)))
                                 .andDo(print());
                } catch (Exception e) {
                    failed.set(true);
                    throw new RuntimeException(e);
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        awaitOnLatch(endLatch);
        AssertionErrors.assertFalse("Exception from withing thread", failed.get());
    }

    private static void awaitOnLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}