package io.github.m1ffer.prices.controller;

import io.github.m1ffer.prices.dto.request.PricePageRequest;
import io.github.m1ffer.prices.dto.request.RangeRequest;
import io.github.m1ffer.prices.dto.response.PriceAverageResponse;
import io.github.m1ffer.prices.dto.response.PricePageResponse;
import io.github.m1ffer.prices.dto.response.PriceResponse;
import io.github.m1ffer.prices.dto.response.PriceTrendResponse;
import io.github.m1ffer.prices.service.PriceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/prices")
@RequiredArgsConstructor
public class PriceController {
    private final PriceService priceService;

    @GetMapping("/last")
    public PriceResponse findLastPrice() {
        return priceService.findLastPrice();
    }

    @GetMapping("/average")
    public PriceAverageResponse findAveragePrice(@Valid RangeRequest range) {
        return priceService.findAveragePrice(range.from(), range.to());
    }

    @GetMapping
    public PricePageResponse getPricePage(@Valid RangeRequest range,
                                          @Valid PricePageRequest pageRequest) {
        return priceService.getPricePage(range.from(), range.to(), pageRequest.page(), pageRequest.size());
    }

    @GetMapping("/trend")
    public PriceTrendResponse findPriceTrend(@Valid RangeRequest range) {
        return priceService.findPriceTrend(range.from(), range.to());
    }
}
