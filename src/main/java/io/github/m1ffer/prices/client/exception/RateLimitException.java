package io.github.m1ffer.prices.client.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

import java.time.Duration;
import java.util.Optional;

public class RateLimitException extends RuntimeException {
    @Getter
    private final HttpStatusCode statusCode;
    private final Duration retryAfter;

    public RateLimitException(String message, HttpStatusCode statusCode, Duration duration) {
        super(message);
        this.statusCode = statusCode;
        this.retryAfter = duration;
    }

    public Optional<Duration> getRetryAfter() {
        return Optional.ofNullable(retryAfter);
    }
}
