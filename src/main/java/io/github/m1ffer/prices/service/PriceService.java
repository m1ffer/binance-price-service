package io.github.m1ffer.prices.service;

import io.github.m1ffer.prices.dto.response.*;
import io.github.m1ffer.prices.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PriceService {
    private final PriceRepository repository;

    public List<String> getAllSymbols() {
        return List.of();
    }

    public ListPriceResponse getLatestPrice(List<String> symbols) {
        return new ListPriceResponse(List.of());
    }

    public PricePageResponse getPage(
            String symbol,
            Instant from,
            Instant to,
            Integer size,
            String cursor,
            Sort.Direction direction
    ) {
        return new PricePageResponse(List.of(), null, size, false);
    }

    public StatisticsPriceResponse getStatistics(String symbol, Instant from, Instant to) {
        return new StatisticsPriceResponse(symbol, from, to, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L);
    }

    public PriceTrendResponse getTrend(String symbol, Instant from, Instant to, String algorithm) {
        return new PriceTrendResponse(symbol, from, to, algorithm, "FLAT", null);
    }

    @Transactional
    public void savePrice(BigDecimal price, Instant fetchedAt) {
        // Stub for scheduler
    }
}
