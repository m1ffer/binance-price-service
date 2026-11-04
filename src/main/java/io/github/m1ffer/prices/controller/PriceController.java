package io.github.m1ffer.prices.controller;

import io.github.m1ffer.prices.dto.request.DateTimeRangeRequest;
import io.github.m1ffer.prices.dto.request.PaginationRequest;
import io.github.m1ffer.prices.dto.response.AveragePriceResponse;
import io.github.m1ffer.prices.dto.response.PricePageResponse;
import io.github.m1ffer.prices.dto.response.PriceResponse;
import io.github.m1ffer.prices.dto.response.PriceTrendResponse;
import io.github.m1ffer.prices.service.PriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/prices")
@RequiredArgsConstructor
public class PriceController implements PriceOpenApi {
    private final PriceService priceService;

    @GetMapping("/latest")
    @Override
    public PriceResponse getLatestPrice() {
        return priceService.findLatestPrice();
    }

    @GetMapping("/average")
    @Override
    public AveragePriceResponse getAveragePrice(@Valid DateTimeRangeRequest range) {
        return priceService.calculateAveragePrice(range.from(), range.to());
    }

    @GetMapping
    @Override
    public PricePageResponse getPrices(
            @Valid DateTimeRangeRequest range,
            @Valid PaginationRequest pageRequest
    ) {
        return priceService.findPrices(
                range.from(), range.to(), pageRequest.page(), pageRequest.size());
    }

    @GetMapping("/trend")
    @Override
    public PriceTrendResponse getPriceTrend(@Valid DateTimeRangeRequest range) {
        return priceService.calculatePriceTrend(range.from(), range.to());
    }
}
