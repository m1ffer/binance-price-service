package io.github.m1ffer.prices.scheduler;

import io.github.m1ffer.prices.client.BinancePriceClient;
import io.github.m1ffer.prices.client.dto.response.FetchedPriceResponse;
import io.github.m1ffer.prices.client.exception.RateLimitException;
import io.github.m1ffer.prices.service.PriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
public class PricePollingScheduler {
    private final BinancePriceClient client;
    private final PriceService priceService;
    private final Duration defaultRateLimitPause;
    private final Clock clock;

    private Instant pauseUntil = Instant.MIN;

    public PricePollingScheduler(
            BinancePriceClient client,
            PriceService priceService,
            @Value("${scheduler.default-rate-limit-pause}") Duration defaultRateLimitPause,
            Clock clock
    ) {
        this.client = client;
        this.priceService = priceService;
        this.defaultRateLimitPause = defaultRateLimitPause;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${scheduler.poll-interval}")
    public void pollAndSavePrice() {
        Instant now = Instant.now(clock);
        if (now.isBefore(pauseUntil)) {
            log.warn("Price fetching paused until {}. Skipping tick.", pauseUntil);
            return;
        }
        try {
            FetchedPriceResponse response = client.fetchAveragePrice();
            priceService.savePrice(response.price(), response.fetchedAt());
            log.info(
                    "Successfully fetched and saved price: {} at {}",
                    response.price(),
                    response.fetchedAt());
        } catch (RateLimitException e) {
            pauseUntil = now.plus(e.getRetryAfter().orElse(defaultRateLimitPause));
            log.warn(
                    "Rate limit hit from Binance. Scheduler paused until {}. Details: {}",
                    pauseUntil,
                    e.getMessage());
        } catch (HttpClientErrorException e) {
            log.error(
                    "Client error from Binance API: status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            log.debug("Details", e);
        } catch (HttpServerErrorException e) {
            log.warn(
                    "Server error from Binance API: status={}, body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            log.debug("Binance server error stack trace", e);
        } catch (ResourceAccessException e) {
            log.warn(
                    "Network or timeout error while contacting Binance API: {}",
                    e.getMessage());
            log.debug("Details", e);
        } catch (Exception e) {
            log.error("Unexpected error during price fetch", e);
        }
    }
}
