package io.github.m1ffer.prices.dto.response;

import java.math.BigDecimal;
import java.util.Objects;

public record PriceAverageResponse(BigDecimal averagePrice) {}
