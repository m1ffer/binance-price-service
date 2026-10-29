package io.github.m1ffer.prices.dto.request;

import jakarta.validation.constraints.*;

import java.time.Instant;

public record PricePageRequest(
        @NotNull(message = "Parameter 'page' must not be null")
        @PositiveOrZero
        Integer page,
        @NotNull(message = "Parameter 'size' must not be null")
        @Positive
        @Max(100)
        Integer size
) {}
