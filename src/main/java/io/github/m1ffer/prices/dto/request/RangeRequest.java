package io.github.m1ffer.prices.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record RangeRequest(
        @NotNull(message = "Parameter 'from' must not be null")
        Instant from,
        @NotNull(message = "Parameter 'to' must not be null")
        Instant to
) {
    @AssertTrue(message = "Parameter 'to' must not be before 'from'")
    public boolean isValidRange() {
        return from == null || to == null || !to.isBefore(from);
    }
}
