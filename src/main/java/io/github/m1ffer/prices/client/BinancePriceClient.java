package io.github.m1ffer.prices.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.m1ffer.prices.client.dto.response.FetchedPriceResponse;
import io.github.m1ffer.prices.client.exception.RateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class BinancePriceClient {
    private static final String AVERAGE_PRICE_PATH = "/api/v3/avgPrice";

    private final RestClient binanceClient;
    private final String symbol;
    private final Clock clock;

    public BinancePriceClient(
            RestClient binanceClient,
            @Value("${app.symbol}") String symbol,
            Clock clock
    ) {
        this.binanceClient = binanceClient;
        this.symbol = symbol;
        this.clock = clock;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record BinanceAveragePriceDto(BigDecimal price) {}

    public FetchedPriceResponse fetchAveragePrice() {
        log.debug("Requesting average price from Binance for symbol: {}", symbol);
        BinanceAveragePriceDto response = binanceClient.get()
                .uri(uriBuilder -> uriBuilder.path(AVERAGE_PRICE_PATH)
                        .queryParam("symbol", symbol)
                        .build())
                .retrieve()
                .onStatus(this::isRateLimitError, this::handleRateLimitError)
                .body(BinanceAveragePriceDto.class);
        log.debug("Received average price: {} for symbol: {}", response.price(), symbol);
        return new FetchedPriceResponse(response.price(), Instant.now(clock));
    }

    private boolean isRateLimitError(HttpStatusCode statusCode) {
        // По спецификации Binance: HTTP 429 возвращается при превышении rate limit,
        // а HTTP 418 — при IP-бане за продолжение отправки запросов после получения 429.
        return statusCode.isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)
                || statusCode.isSameCodeAs(HttpStatus.I_AM_A_TEAPOT);
    }

    private void handleRateLimitError(
            HttpRequest request,
            ClientHttpResponse response
    ) throws IOException {
        String message = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        HttpStatusCode statusCode = response.getStatusCode();
        String rawHeader = response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        Duration retryAfter = rawHeader == null
                ? null
                : Duration.ofSeconds(Long.parseLong(rawHeader));
        throw new RateLimitException(message, statusCode, retryAfter);
    }
}
