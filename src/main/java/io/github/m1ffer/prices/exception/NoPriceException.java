package io.github.m1ffer.prices.exception;

import java.time.Instant;

public class NoPriceException extends RuntimeException {
    public NoPriceException(String message) {
        super(message);
    }

    public static NoPriceException forSymbol(String symbol){
        return new NoPriceException("No prices found for symbol '%s'".formatted(symbol));
    }

    public static NoPriceException forRange(String symbol, Instant from, Instant to){
        return new NoPriceException(
                "No prices found for symbol '%s' in range [%s, %s]" .formatted(symbol, from, to)
        );
    }
}
