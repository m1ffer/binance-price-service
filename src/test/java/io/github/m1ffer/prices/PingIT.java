package io.github.m1ffer.prices;

import io.github.m1ffer.prices.model.Ping;
import io.github.m1ffer.prices.repository.PingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PingIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", postgres::getJdbcUrl);
        registry.add("DB_USER", postgres::getUsername);
        registry.add("DB_PASSWORD", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PingRepository pingRepository;

    @BeforeEach
    void setUp() {
        pingRepository.deleteAll();
    }

    @Test
    void shouldSaveAndRetrievePingWithPostgreSQL() throws Exception {
        // When no pings exist, /last-ping returns "No pings yet"
        mockMvc.perform(get("/last-ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("No pings yet"));

        // Call /ping to save a new ping into PostgreSQL
        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.millis").isNumber());

        // Verify that the ping was actually persisted in PostgreSQL
        List<Ping> pings = pingRepository.findAll();
        assertThat(pings).hasSize(1);
        long savedTime = pings.get(0).getTime();

        // Call /last-ping and verify that it returns the saved ping
        mockMvc.perform(get("/last-ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pings.get(0).getId()))
                .andExpect(jsonPath("$.millis").value(savedTime));
    }
}
