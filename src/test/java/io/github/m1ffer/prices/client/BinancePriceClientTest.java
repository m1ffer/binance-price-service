package io.github.m1ffer.prices.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.m1ffer.prices.client.dto.response.FetchedPriceResponse;
import io.github.m1ffer.prices.client.exception.RateLimitException;
import io.github.m1ffer.prices.client.fixture.BinanceFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

class BinancePriceClientTest {

    private static final String SYMBOL = "Some symbol";
    private static final BigDecimal PRICE = BigDecimal.ONE;
    private static final int MINS = 1;
    private static final long CLOSE_TIME = 1L;
    private static final long RETRY_AFTER = 60L;
    private static final long BANNED_UNTIL = 1L;
    private static final Instant FETCHED_AT = Instant.parse("2026-01-01T12:00:00Z");
    private static final Clock CLOCK = Clock.fixed(FETCHED_AT, ZoneOffset.UTC);

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private BinancePriceClient client;

    @BeforeEach
    void setUp() {
        RestClient restClient = RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory())
                .baseUrl(wireMock.baseUrl())
                .build();
        client = new BinancePriceClient(restClient, SYMBOL, CLOCK);
    }

    @Test
    void fetchAveragePriceSuccess() {
        BinanceFixtures.stubAvgPrice(wireMock, SYMBOL, MINS, PRICE, CLOSE_TIME);

        FetchedPriceResponse response = client.fetchAveragePrice();

        assertNotNull(response);
        assertEquals(PRICE, response.price());
        assertEquals(FETCHED_AT, response.fetchedAt());
    }

    @Test
    void fetchAveragePriceRateLimitExceeded() {
        BinanceFixtures.stubRateLimit(wireMock, SYMBOL, RETRY_AFTER);

        RateLimitException exception = assertThrows(
                RateLimitException.class, () -> client.fetchAveragePrice());

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatusCode());
        assertEquals(BinanceFixtures.rateLimitJson(), exception.getMessage());
        assertTrue(exception.getRetryAfter().isPresent());
        assertEquals(Duration.ofSeconds(RETRY_AFTER), exception.getRetryAfter().get());
    }

    @Test
    void fetchAveragePriceTeapotRateLimit() {
        BinanceFixtures.stubIpBanned(wireMock, SYMBOL, RETRY_AFTER, BANNED_UNTIL);

        RateLimitException exception = assertThrows(
                RateLimitException.class, () -> client.fetchAveragePrice());

        assertEquals(HttpStatus.I_AM_A_TEAPOT, exception.getStatusCode());
        assertEquals(BinanceFixtures.ipBannedJson(BANNED_UNTIL), exception.getMessage());
        assertTrue(exception.getRetryAfter().isPresent());
        assertEquals(Duration.ofSeconds(RETRY_AFTER), exception.getRetryAfter().get());
    }
}
