package io.github.m1ffer.prices.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record SymbolRequest(
        @NotEmpty(message = "Parameter 'symbol' must be not empty")
        String symbol
) {
}
