package io.github.m1ffer.prices.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record StatisticsPriceResponse(
        String symbol,
        Instant from,
        Instant to,
        BigDecimal average,
        BigDecimal min,
        BigDecimal max,
        Long count
) {}
