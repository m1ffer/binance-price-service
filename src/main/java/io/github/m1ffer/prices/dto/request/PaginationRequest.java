package io.github.m1ffer.prices.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record PaginationRequest(
        @NotNull(message = "Parameter 'page' must not be null")
        @PositiveOrZero(message = "Parameter 'page' must be positive or zero")
        Integer page,
        @NotNull(message = "Parameter 'size' must not be null")
        @Positive(message = "Parameter 'size' must be positive")
        @Max(
                value = MAX_PAGE_SIZE,
                message = "Parameter 'size' must be less or equal than " + MAX_PAGE_SIZE)
        Integer size
) {
    public static final int MAX_PAGE_SIZE = 100;
}
