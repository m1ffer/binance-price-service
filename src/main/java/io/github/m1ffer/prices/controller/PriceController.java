package io.github.m1ffer.prices.controller;

import io.github.m1ffer.prices.dto.request.DateTimeRangeRequest;
import io.github.m1ffer.prices.dto.request.SymbolRequest;
import io.github.m1ffer.prices.dto.response.*;
import io.github.m1ffer.prices.service.PriceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prices")
@RequiredArgsConstructor
public class PriceController {
    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    private final PriceService priceService;

    @GetMapping("/symbols")
    public List<String> getAllSymbols() {
        return priceService.getAllSymbols();
    }

    @GetMapping("/latest")
    public ListPriceResponse getLatestPrice(
            @RequestParam(required = false)
            List<@Valid SymbolRequest> symbols
    ) {
        List<String> symbolsList = symbols == null ? null : symbols.stream()
                        .map(SymbolRequest::symbol)
                        .toList();
        return priceService.getLatestPrice(symbolsList);
    }

    @GetMapping
    public PricePageResponse getPrices(
            @Valid SymbolRequest symbolRequest,
            @Valid DateTimeRangeRequest range,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE)
            @Positive
            @Max(MAX_PAGE_SIZE)
            Integer size,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) Sort.Direction direction
    ) {
        return priceService.getPage(
                symbolRequest.symbol(),
                range.from(),
                range.to(),
                size,
                cursor,
                direction
        );
    }

    @GetMapping("/statistics")
    public StatisticsPriceResponse getStatistics(
            @Valid SymbolRequest symbolRequest,
            @Valid DateTimeRangeRequest range
    ) {
        return priceService.getStatistics(
                symbolRequest.symbol(),
                range.from(),
                range.to()
        );
    }

    @GetMapping("/trend")
    public PriceTrendResponse getPriceTrend(
            @Valid SymbolRequest symbolRequest,
            @Valid DateTimeRangeRequest range,
            @RequestParam(required = false) String algorithm
    ) {
        return priceService.getTrend(
                symbolRequest.symbol(),
                range.from(),
                range.to(),
                algorithm
        );
    }
}
