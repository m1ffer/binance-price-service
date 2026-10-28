package io.github.m1ffer.prices.dto.response;

import java.time.Instant;
import java.util.Objects;

public record PriceTrendResponse(String symbol, Instant startDate, Instant endDate, Trend trend){
    public enum Trend{
        UPWARD, DOWNWARD, FLAT
    }
}
