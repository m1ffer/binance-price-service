package io.github.m1ffer.prices.exception;

import java.time.Instant;

public class PriceNotFoundException extends RuntimeException {
    public PriceNotFoundException(String message) {
        super(message);
    }

    public static PriceNotFoundException forSymbol(String symbol) {
        return new PriceNotFoundException("No prices found for symbol '%s'".formatted(symbol));
    }

    public static PriceNotFoundException forRange(String symbol, Instant from, Instant to) {
        return new PriceNotFoundException(
                "No prices found for symbol '%s' in range [%s, %s]".formatted(symbol, from, to));
    }
}
