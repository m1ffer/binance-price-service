package io.github.m1ffer.prices.service;

import io.github.m1ffer.prices.dto.response.AveragePriceResponse;
import io.github.m1ffer.prices.dto.response.PricePageResponse;
import io.github.m1ffer.prices.dto.response.PriceResponse;
import io.github.m1ffer.prices.dto.response.PriceTrendResponse;
import io.github.m1ffer.prices.exception.PriceNotFoundException;
import io.github.m1ffer.prices.model.Price;
import io.github.m1ffer.prices.repository.PriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    private static final String SYMBOL = "Some symbol";
    private static final BigDecimal LOW_PRICE = BigDecimal.ONE;
    private static final BigDecimal HIGH_PRICE = BigDecimal.TWO;
    private static final BigDecimal PRICE = LOW_PRICE;
    private static final Instant FROM = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-01-01T12:00:00Z");
    private static final Instant FETCHED_AT = FROM;

    @Mock
    private PriceRepository repository;

    private PriceService priceService;

    @BeforeEach
    void setUp() {
        priceService = new PriceService(repository, SYMBOL);
    }

    @Test
    void findLatestPriceSuccess() {
        Price price = new Price(SYMBOL, PRICE, FETCHED_AT);
        when(repository.findFirstBySymbolOrderByFetchedAtDesc(SYMBOL))
                .thenReturn(Optional.of(price));
        PriceResponse response = priceService.findLatestPrice();
        assertNotNull(response);
        assertEquals(SYMBOL, response.symbol());
        assertEquals(PRICE, response.price());
        assertEquals(FETCHED_AT, response.fetchedAt());
        verify(repository).findFirstBySymbolOrderByFetchedAtDesc(SYMBOL);
    }

    @Test
    void findLatestPriceNotFound() {
        when(repository.findFirstBySymbolOrderByFetchedAtDesc(SYMBOL)).thenReturn(Optional.empty());
        assertThrows(PriceNotFoundException.class, () -> priceService.findLatestPrice());
        verify(repository).findFirstBySymbolOrderByFetchedAtDesc(SYMBOL);
    }

    @Test
    void findPricesSuccess() {
        int pageNumber = 0;
        int pageSize = 2;
        long totalElements = 2;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        Price price1 = new Price(SYMBOL, LOW_PRICE, FROM);
        Price price2 = new Price(SYMBOL, HIGH_PRICE, TO);
        Page<Price> pageFromDb =
                new PageImpl<>(List.of(price1, price2), pageRequest, totalElements);
        when(repository.findAllBySymbolAndFetchedAtBetweenOrderByFetchedAtDesc(
                SYMBOL, FROM, TO, pageRequest)).thenReturn(pageFromDb);
        PricePageResponse response = priceService.findPrices(FROM, TO, pageNumber, pageSize);
        assertNotNull(response);
        assertEquals(pageNumber, response.page());
        assertEquals(pageSize, response.size());
        assertEquals(totalElements, response.totalElements());
        assertFalse(response.hasNext());
        assertEquals(pageSize, response.items().size());
        assertEquals(LOW_PRICE, response.items().getFirst().price());
    }

    @Test
    void findPricesEmpty() {
        int pageNumber = 0;
        int pageSize = 10;
        long totalElements = 0;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        Page<Price> emptyPage = new PageImpl<>(List.of(), pageRequest, totalElements);
        when(repository.findAllBySymbolAndFetchedAtBetweenOrderByFetchedAtDesc(
                SYMBOL, FROM, TO, pageRequest)).thenReturn(emptyPage);
        PricePageResponse response = priceService.findPrices(FROM, TO, pageNumber, pageSize);
        assertNotNull(response);
        assertTrue(response.items().isEmpty());
        assertEquals(totalElements, response.totalElements());
        assertFalse(response.hasNext());
    }

    @Test
    void calculateAveragePriceSuccess() {
        when(repository.findAveragePrice(SYMBOL, FROM, TO)).thenReturn(Optional.of(PRICE));
        AveragePriceResponse response = priceService.calculateAveragePrice(FROM, TO);
        assertNotNull(response);
        assertEquals(PRICE, response.averagePrice());
        verify(repository).findAveragePrice(SYMBOL, FROM, TO);
    }

    @Test
    void calculateAveragePriceNotFound() {
        when(repository.findAveragePrice(SYMBOL, FROM, TO)).thenReturn(Optional.empty());
        assertThrows(
                PriceNotFoundException.class,
                () -> priceService.calculateAveragePrice(FROM, TO));
        verify(repository).findAveragePrice(SYMBOL, FROM, TO);
    }

    @Test
    void calculatePriceTrendUpward() {
        Price startPrice = new Price(SYMBOL, LOW_PRICE, FROM);
        Price endPrice = new Price(SYMBOL, HIGH_PRICE, TO);
        when(repository.findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtAsc(
                SYMBOL, FROM, TO)).thenReturn(Optional.of(startPrice));
        when(repository.findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtDesc(
                SYMBOL, FROM, TO)).thenReturn(Optional.of(endPrice));
        PriceTrendResponse response = priceService.calculatePriceTrend(FROM, TO);
        assertNotNull(response);
        assertEquals(PriceTrendResponse.Trend.UPWARD, response.trend());
        assertEquals(SYMBOL, response.symbol());
        assertEquals(FROM, response.startDate());
        assertEquals(TO, response.endDate());
    }

    @Test
    void calculatePriceTrendDownward() {
        Price startPrice = new Price(SYMBOL, HIGH_PRICE, FROM);
        Price endPrice = new Price(SYMBOL, LOW_PRICE, TO);
        when(repository.findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtAsc(
                SYMBOL, FROM, TO)).thenReturn(Optional.of(startPrice));
        when(repository.findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtDesc(
                SYMBOL, FROM, TO)).thenReturn(Optional.of(endPrice));
        PriceTrendResponse response = priceService.calculatePriceTrend(FROM, TO);
        assertNotNull(response);
        assertEquals(PriceTrendResponse.Trend.DOWNWARD, response.trend());
    }

    @Test
    void calculatePriceTrendFlat() {
        Price startPrice = new Price(SYMBOL, PRICE, FROM);
        Price endPrice = new Price(SYMBOL, PRICE, TO);
        when(repository.findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtAsc(
                SYMBOL, FROM, TO)).thenReturn(Optional.of(startPrice));
        when(repository.findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtDesc(
                SYMBOL, FROM, TO)).thenReturn(Optional.of(endPrice));
        PriceTrendResponse response = priceService.calculatePriceTrend(FROM, TO);
        assertNotNull(response);
        assertEquals(PriceTrendResponse.Trend.FLAT, response.trend());
    }

    @Test
    void calculatePriceTrendStartNotFound() {
        when(repository.findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtAsc(
                SYMBOL, FROM, TO)).thenReturn(Optional.empty());
        assertThrows(
                PriceNotFoundException.class,
                () -> priceService.calculatePriceTrend(FROM, TO));
    }

    @Test
    void savePriceSuccess() {
        priceService.savePrice(PRICE, FETCHED_AT);
        ArgumentCaptor<Price> captor = ArgumentCaptor.forClass(Price.class);
        verify(repository).save(captor.capture());
        Price savedPrice = captor.getValue();
        assertNotNull(savedPrice);
        assertEquals(SYMBOL, savedPrice.getSymbol());
        assertEquals(PRICE, savedPrice.getPrice());
        assertEquals(FETCHED_AT, savedPrice.getFetchedAt());
    }
}
