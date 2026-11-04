package io.github.m1ffer.prices.client.fixture;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public final class BinanceFixtures {
    private BinanceFixtures() {}

    private static final String AVG_PRICE_PATH = "/api/v3/avgPrice";
    private static final String SYMBOL_PARAM = "symbol";

    public static final int BINANCE_WEIGHT_LIMIT = 1200;
    public static final int BINANCE_RATE_LIMIT_CODE = -1003;

    public static final String AVG_PRICE_RESPONSE_PATTERN = """
            {
                "mins": %d,
                "price": "%s",
                "closeTime": %d
            }
            """;

    public static final String RATE_LIMIT_RESPONSE_PATTERN = """
            {
                "code": %d,
                "msg": "Too much request weight used; current limit is %d request weight per minute. Please use the websocket for live updates to avoid bans."
            }
            """;

    public static final String IP_BANNED_RESPONSE_PATTERN = """
            {
                "code": %d,
                "msg": "Way too much request weight used; IP banned until %d. Please use the websocket for live updates to avoid bans."
            }
            """;

    public static String avgPriceJson(int mins, BigDecimal price, long closeTime) {
        return AVG_PRICE_RESPONSE_PATTERN.formatted(mins, price, closeTime);
    }

    public static String rateLimitJson() {
        return RATE_LIMIT_RESPONSE_PATTERN.formatted(BINANCE_RATE_LIMIT_CODE, BINANCE_WEIGHT_LIMIT);
    }

    public static String ipBannedJson(long bannedUntilTimestamp) {
        return IP_BANNED_RESPONSE_PATTERN.formatted(BINANCE_RATE_LIMIT_CODE, bannedUntilTimestamp);
    }

    public static void stubAvgPrice(
            WireMockExtension wireMock,
            String symbol,
            int mins,
            BigDecimal price,
            long closeTime
    ) {
        wireMock.stubFor(get(urlPathEqualTo(AVG_PRICE_PATH))
                .withQueryParam(SYMBOL_PARAM, equalTo(symbol))
                .willReturn(okJson(avgPriceJson(mins, price, closeTime))));
    }

    public static void stubRateLimit(
            WireMockExtension wireMock,
            String symbol,
            long retryAfterSeconds
    ) {
        wireMock.stubFor(get(urlPathEqualTo(AVG_PRICE_PATH))
                .withQueryParam(SYMBOL_PARAM, equalTo(symbol))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.TOO_MANY_REQUESTS.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds))
                        .withBody(rateLimitJson())));
    }

    public static void stubIpBanned(
            WireMockExtension wireMock,
            String symbol,
            long retryAfterSeconds,
            long bannedUntilTimestamp
    ) {
        wireMock.stubFor(get(urlPathEqualTo(AVG_PRICE_PATH))
                .withQueryParam(SYMBOL_PARAM, equalTo(symbol))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.I_AM_A_TEAPOT.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .withHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds))
                        .withBody(ipBannedJson(bannedUntilTimestamp))));
    }
}
