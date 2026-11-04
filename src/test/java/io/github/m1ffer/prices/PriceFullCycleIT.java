package io.github.m1ffer.prices;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.m1ffer.prices.client.fixture.BinanceFixtures;
import io.github.m1ffer.prices.dto.response.PriceTrendResponse;
import io.github.m1ffer.prices.repository.PriceRepository;
import io.github.m1ffer.prices.scheduler.PricePollingScheduler;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PriceFullCycleIT {

    private static final String SYMBOL = "BNBUSDT";
    private static final BigDecimal LOW_PRICE = new BigDecimal("600.00");
    private static final BigDecimal HIGH_PRICE = new BigDecimal("700.00");
    private static final int MINS = 5;
    private static final long CLOSE_TIME = 1761984000000L;
    private static final Instant FIRST_FETCH_TIME = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant SECOND_FETCH_TIME = Instant.parse("2026-01-01T10:01:00Z");

    static class MutableTestClock extends Clock {
        @Setter
        private Instant instant;
        @Getter
        private final ZoneId zone;

        public MutableTestClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableTestClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }

    @TestConfiguration
    static class TestClockConfig {
        @Bean
        @Primary
        MutableTestClock mutableTestClock() {
            return new MutableTestClock(FIRST_FETCH_TIME, ZoneOffset.UTC);
        }
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", postgres::getJdbcUrl);
        registry.add("DB_USER", postgres::getUsername);
        registry.add("DB_PASSWORD", postgres::getPassword);
        registry.add("BINANCE_SYMBOL", () -> SYMBOL);
        registry.add("BINANCE_BASE_URL", wireMock::baseUrl);
        registry.add("scheduler.enabled", () -> false);
    }

    @Autowired
    private MutableTestClock clock;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PricePollingScheduler scheduler;

    @Autowired
    private PriceRepository repository;

    @BeforeEach
    void cleanUp() {
        clock.setInstant(FIRST_FETCH_TIME);
        repository.deleteAll();
        wireMock.resetAll();
    }

    @Test
    void fullCyclePriceFlowSuccess() throws Exception {
        BinanceFixtures.stubAvgPrice(wireMock, SYMBOL, MINS, LOW_PRICE, CLOSE_TIME);
        scheduler.pollAndSavePrice();
        mockMvc.perform(get("/api/v1/prices/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value(SYMBOL))
                .andExpect(jsonPath("$.price").value(LOW_PRICE.doubleValue()))
                .andExpect(jsonPath("$.fetchedAt").value(FIRST_FETCH_TIME.toString()));
        clock.setInstant(SECOND_FETCH_TIME);
        BinanceFixtures.stubAvgPrice(wireMock, SYMBOL, MINS, HIGH_PRICE, CLOSE_TIME);
        scheduler.pollAndSavePrice();
        mockMvc.perform(get("/api/v1/prices/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value(SYMBOL))
                .andExpect(jsonPath("$.price").value(HIGH_PRICE.doubleValue()))
                .andExpect(jsonPath("$.fetchedAt").value(SECOND_FETCH_TIME.toString()));

        BigDecimal averagePrice = LOW_PRICE.add(HIGH_PRICE).divide(BigDecimal.TWO);
        mockMvc.perform(get("/api/v1/prices/average")
                        .param("from", FIRST_FETCH_TIME.toString())
                        .param("to", SECOND_FETCH_TIME.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averagePrice").value(averagePrice.doubleValue()));
        mockMvc.perform(get("/api/v1/prices")
                        .param("from", FIRST_FETCH_TIME.toString())
                        .param("to", SECOND_FETCH_TIME.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].price").value(HIGH_PRICE.doubleValue()))
                .andExpect(jsonPath("$.items[0].fetchedAt").value(SECOND_FETCH_TIME.toString()))
                .andExpect(jsonPath("$.items[1].price").value(LOW_PRICE.doubleValue()))
                .andExpect(jsonPath("$.items[1].fetchedAt").value(FIRST_FETCH_TIME.toString()));
        mockMvc.perform(get("/api/v1/prices/trend")
                        .param("from", FIRST_FETCH_TIME.toString())
                        .param("to", SECOND_FETCH_TIME.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value(SYMBOL))
                .andExpect(jsonPath("$.trend").value(PriceTrendResponse.Trend.UPWARD.toString()));
    }
}
