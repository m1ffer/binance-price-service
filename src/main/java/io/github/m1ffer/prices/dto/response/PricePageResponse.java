package io.github.m1ffer.prices.dto.response;

import java.util.List;

public record PricePageResponse(
        List<PriceResponse> items,
        String nextCursor,
        int size,
        boolean hasNext
) {
}
