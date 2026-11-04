package io.github.m1ffer.prices.client.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record FetchedPriceResponse(BigDecimal price, Instant fetchedAt) {
}
