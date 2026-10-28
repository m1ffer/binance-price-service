package io.github.m1ffer.prices.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PricePageResponse(List<PriceResponse> items, int page, int size, long totalElements, boolean hasNext) {
    public static PricePageResponse fromPage(Page<PriceResponse> page){
        return new PricePageResponse(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.hasNext());
    }
}
