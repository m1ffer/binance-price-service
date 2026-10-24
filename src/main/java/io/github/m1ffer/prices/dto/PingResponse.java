package io.github.m1ffer.prices.dto;

import io.github.m1ffer.prices.model.Ping;

public record PingResponse(long id, long millis) {
    public PingResponse(Ping ping) {
        this(ping.getId(), ping.getTime());
    }
}
