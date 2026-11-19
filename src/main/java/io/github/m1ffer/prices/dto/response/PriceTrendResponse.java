package io.github.m1ffer.prices.dto.response;

import java.time.Instant;

public record PriceTrendResponse(
        String symbol,
        Instant from,
        Instant to,
        String algorithm,
        String trend,
        PriceTrendDetails details
) {}
