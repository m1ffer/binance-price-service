package io.github.m1ffer.prices.dto.response;

import io.github.m1ffer.prices.model.Price;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceResponse(String symbol, BigDecimal price, Instant fetchedAt) {
    public static PriceResponse from(Price price) {
        return new PriceResponse(price.getSymbol(), price.getPrice(), price.getFetchedAt());
    }
}
