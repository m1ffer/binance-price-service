package io.github.m1ffer.prices.service;

import io.github.m1ffer.prices.dto.response.PriceAverageResponse;
import io.github.m1ffer.prices.dto.response.PricePageResponse;
import io.github.m1ffer.prices.dto.response.PriceResponse;
import io.github.m1ffer.prices.dto.response.PriceTrendResponse;
import io.github.m1ffer.prices.model.Price;
import io.github.m1ffer.prices.repository.PriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class PriceServiceIntegrationTest {

    private static final String SYMBOL = "BNBUSDT";
    private static final BigDecimal LOW_PRICE = BigDecimal.ONE;
    private static final BigDecimal HIGH_PRICE = BigDecimal.TWO;
    private static final BigDecimal PRICE = LOW_PRICE;
    private static final Instant FROM = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-01-01T12:00:00Z");
    private static final Instant FETCHED_AT = TO;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", postgres::getJdbcUrl);
        registry.add("DB_USER", postgres::getUsername);
        registry.add("DB_PASSWORD", postgres::getPassword);
        registry.add("BINANCE_SYMBOL", () -> SYMBOL);
    }

    @Autowired
    private PriceService priceService;

    @Autowired
    private PriceRepository repository;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    void saveAndFindLastPrice() {
        priceService.savePrice(PRICE, FETCHED_AT);
        PriceResponse response = priceService.findLastPrice();
        assertNotNull(response);
        assertEquals(SYMBOL, response.symbol());
        assertEquals(0, PRICE.compareTo(response.price()));
        assertEquals(FETCHED_AT, response.fetchedAt());
    }

    @Test
    void findAveragePrice() {
        repository.save(new Price(SYMBOL, LOW_PRICE, FROM));
        repository.save(new Price(SYMBOL, HIGH_PRICE, TO));
        PriceAverageResponse response = priceService.findAveragePrice(FROM, TO);
        assertNotNull(response);
        BigDecimal averagePrice = LOW_PRICE.add(HIGH_PRICE).divide(BigDecimal.TWO);
        assertEquals(0, averagePrice.compareTo(response.averagePrice()));
    }

    @Test
    void getPricePage() {
        int pageNumber = 0;
        int pageSize = 10;
        long totalElements = 2;
        repository.save(new Price(SYMBOL, LOW_PRICE, FROM));
        repository.save(new Price(SYMBOL, HIGH_PRICE, TO));
        PricePageResponse response = priceService.getPricePage(FROM, TO, pageNumber, pageSize);
        assertNotNull(response);
        assertEquals(totalElements, response.totalElements());
        assertEquals(2, response.items().size());
        assertFalse(response.hasNext());
    }

    @Test
    void findPriceTrend() {
        repository.save(new Price(SYMBOL, LOW_PRICE, FROM));
        repository.save(new Price(SYMBOL, HIGH_PRICE, TO));
        PriceTrendResponse response = priceService.findPriceTrend(FROM, TO);
        assertNotNull(response);
        assertEquals(PriceTrendResponse.Trend.UPWARD, response.trend());
        assertEquals(SYMBOL, response.symbol());
    }
}
