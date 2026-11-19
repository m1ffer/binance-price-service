package io.github.m1ffer.prices.dto.response;

import io.github.m1ffer.prices.model.Price;

import java.util.List;

public record ListPriceResponse(List<PriceResponse> prices) {
    public static ListPriceResponse fromPrices(List<Price> prices) {
        return new ListPriceResponse(prices.stream()
                .map(PriceResponse::fromPrice)
                .toList());
    }
}
