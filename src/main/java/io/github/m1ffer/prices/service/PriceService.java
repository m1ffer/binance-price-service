package io.github.m1ffer.prices.service;

import io.github.m1ffer.prices.dto.response.PriceAverageResponse;
import io.github.m1ffer.prices.dto.response.PricePageResponse;
import io.github.m1ffer.prices.dto.response.PriceResponse;
import io.github.m1ffer.prices.dto.response.PriceTrendResponse;
import io.github.m1ffer.prices.exception.NoPriceException;
import io.github.m1ffer.prices.model.Price;
import io.github.m1ffer.prices.repository.PriceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class PriceService {
    private final PriceRepository repository;
    private final String symbol;

    public PriceService(PriceRepository repository, @Value("${binance.symbol}") String symbol){
        this.repository = repository;
        this.symbol = symbol;
    }

    public PriceResponse findLastPrice(){
        return repository.findFirstBySymbolOrderByFetchedAtDesc(symbol)
                .map(PriceResponse::fromPrice)
                .orElseThrow(() -> NoPriceException.forSymbol(symbol));
    }

    public PricePageResponse getPricePage(Instant from, Instant to, int pageNumber, int pageSize){
        PageRequest configuration = PageRequest.of(pageNumber, pageSize);
        Page<PriceResponse> page = repository
                .findAllBySymbolAndFetchedAtBetweenOrderByFetchedAtDesc(symbol, from, to, configuration)
                .map(PriceResponse::fromPrice);
        return PricePageResponse.fromPage(page);
    }

    public PriceAverageResponse findAveragePrice(Instant from, Instant to){
        return repository.findAveragePrice(symbol, from, to)
                .map(PriceAverageResponse::new)
                .orElseThrow(() -> NoPriceException.forRange(symbol, from, to));
    }

    public PriceTrendResponse findPriceTrend(Instant from, Instant to){
        Price startPrice = repository.findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtAsc(symbol, from, to)
                .orElseThrow(() -> NoPriceException.forRange(symbol, from, to));
        Price endPrice = repository.findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtDesc(symbol, from, to)
                .orElseThrow(() -> new IllegalStateException(
                        "End price not found for symbol '%s' in range [%s, %s] though start price exists"
                                .formatted(symbol, from, to)
                ));
        PriceTrendResponse.Trend trend = switch(startPrice.getPrice().compareTo(endPrice.getPrice())){
            case 0 -> PriceTrendResponse.Trend.FLAT;
            case 1 -> PriceTrendResponse.Trend.DOWNWARD;
            case -1 -> PriceTrendResponse.Trend.UPWARD;
            default ->
                    throw new IllegalStateException("Unexpected value: " + startPrice.getPrice().compareTo(endPrice.getPrice()));
        };
        return new PriceTrendResponse(symbol, from, to, trend);
    }

    @Transactional
    public void savePrice(BigDecimal price, Instant fetchedAt){
        repository.save(new Price(symbol, price, fetchedAt));
    }
}
