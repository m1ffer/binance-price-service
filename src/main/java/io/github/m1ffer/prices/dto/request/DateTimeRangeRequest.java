package io.github.m1ffer.prices.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record DateTimeRangeRequest(
        @NotNull(message = "Parameter 'from' must not be null")
        Instant from,
        @NotNull(message = "Parameter 'to' must not be null")
        Instant to
) {
    public static final String MIN_SUPPORTED_DATE_STRING = "1970-01-01T00:00:00Z";
    public static final String MAX_SUPPORTED_DATE_STRING = "9999-12-31T23:59:59Z";
    public static final Instant MIN_SUPPORTED_DATE = Instant.parse(MIN_SUPPORTED_DATE_STRING);
    public static final Instant MAX_SUPPORTED_DATE = Instant.parse(MAX_SUPPORTED_DATE_STRING);
    @AssertTrue(message = "Parameter 'to' must not be before 'from'")
    public boolean isValidRange() {
        return from == null || to == null || !to.isBefore(from);
    }

    @AssertTrue(message = "Parameter 'from' must be between " + MIN_SUPPORTED_DATE_STRING
            + " and " + MAX_SUPPORTED_DATE_STRING)
    public boolean isFromWithinSupportedRange() {
        return from == null || !from.isBefore(MIN_SUPPORTED_DATE) && !from.isAfter(MAX_SUPPORTED_DATE);
    }

    @AssertTrue(message = "Parameter 'to' must be between " + MIN_SUPPORTED_DATE_STRING
            + " and " + MAX_SUPPORTED_DATE_STRING)
    public boolean isToWithinSupportedRange() {
        return to == null || !to.isBefore(MIN_SUPPORTED_DATE) && !to.isAfter(MAX_SUPPORTED_DATE);
    }
}
