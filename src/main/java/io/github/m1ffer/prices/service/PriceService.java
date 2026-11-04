package io.github.m1ffer.prices.service;

import io.github.m1ffer.prices.dto.response.AveragePriceResponse;
import io.github.m1ffer.prices.dto.response.PricePageResponse;
import io.github.m1ffer.prices.dto.response.PriceResponse;
import io.github.m1ffer.prices.dto.response.PriceTrendResponse;
import io.github.m1ffer.prices.exception.PriceNotFoundException;
import io.github.m1ffer.prices.model.Price;
import io.github.m1ffer.prices.repository.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PriceService {
    private final PriceRepository repository;
    private final String symbol;

    public PriceService(PriceRepository repository, @Value("${app.symbol}") String symbol) {
        this.repository = repository;
        this.symbol = symbol;
    }

    public PriceResponse findLatestPrice() {
        PriceResponse response = repository.findFirstBySymbolOrderByFetchedAtDesc(symbol)
                .map(PriceResponse::from)
                .orElseThrow(() -> PriceNotFoundException.forSymbol(symbol));
        log.debug("Found latest price for symbol '{}': {}", symbol, response.price());
        return response;
    }

    public PricePageResponse findPrices(Instant from, Instant to, int pageNumber, int pageSize) {
        PageRequest configuration = PageRequest.of(pageNumber, pageSize);
        Page<PriceResponse> page = repository
                .findAllBySymbolAndFetchedAtBetweenOrderByFetchedAtDesc(
                        symbol, from, to, configuration)
                .map(PriceResponse::from);
        log.debug("Found {} prices (page {}/{}) for symbol '{}'",
                page.getNumberOfElements(), pageNumber, page.getTotalPages(), symbol);
        return PricePageResponse.from(page);
    }

    public AveragePriceResponse calculateAveragePrice(Instant from, Instant to) {
        AveragePriceResponse response = repository.findAveragePrice(symbol, from, to)
                .map(avg -> avg.setScale(Price.PRICE_SCALE, RoundingMode.HALF_UP).stripTrailingZeros())
                .map(AveragePriceResponse::new)
                .orElseThrow(() -> PriceNotFoundException.forRange(symbol, from, to));
        log.debug("Calculated average price: {} for symbol: {} in range [{}, {}]",
                response.averagePrice(), symbol, from, to);
        return response;
    }

    public PriceTrendResponse calculatePriceTrend(Instant from, Instant to) {
        Price startPrice = repository
                .findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtAsc(symbol, from, to)
                .orElseThrow(() -> PriceNotFoundException.forRange(symbol, from, to));
        Price endPrice = repository.findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtDesc(
                symbol, from, to)
                .orElseThrow(() -> new IllegalStateException(
                "End price not found for symbol '%s' in range [%s, %s] though start price exists"
                        .formatted(symbol, from, to)));
        PriceTrendResponse.Trend trend =
                switch (startPrice.getPrice().compareTo(endPrice.getPrice())) {
                    case 0 -> PriceTrendResponse.Trend.FLAT;
                    case 1 -> PriceTrendResponse.Trend.DOWNWARD;
                    case -1 -> PriceTrendResponse.Trend.UPWARD;
                    default -> throw new IllegalStateException(
                            "Unexpected value: "
                                    + startPrice.getPrice().compareTo(endPrice.getPrice()));
                };
        log.debug("Calculated price trend: {} for symbol: {} in range [{}, {}]",
                trend, symbol, from, to);
        return new PriceTrendResponse(symbol, from, to, trend);
    }

    @Transactional
    public void savePrice(BigDecimal price, Instant fetchedAt) {
        log.debug("Persisting price: {} for symbol: {} at {}", price, symbol, fetchedAt);
        repository.save(new Price(symbol, price, fetchedAt));
    }
}
