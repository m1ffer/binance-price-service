package io.github.m1ffer.prices.service;

import io.github.m1ffer.prices.dto.response.PriceAverageResponse;
import io.github.m1ffer.prices.dto.response.PricePageResponse;
import io.github.m1ffer.prices.dto.response.PriceResponse;
import io.github.m1ffer.prices.dto.response.PriceTrendResponse;
import io.github.m1ffer.prices.exception.NoPriceException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceServiceUnitTest {

    private static final String SYMBOL = "Some symbol";
    private static final BigDecimal LOW_PRICE = new BigDecimal("1.00");
    private static final BigDecimal HIGH_PRICE = new BigDecimal("2.00");
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
    void findLastPriceSuccess() {
        Price price = new Price(SYMBOL, PRICE, FETCHED_AT);
        when(repository.findFirstBySymbolOrderByFetchedAtDesc(SYMBOL)).thenReturn(Optional.of(price));
        PriceResponse response = priceService.findLastPrice();
        assertNotNull(response);
        assertEquals(SYMBOL, response.symbol());
        assertEquals(PRICE, response.price());
        assertEquals(FETCHED_AT, response.fetchedAt());
        verify(repository).findFirstBySymbolOrderByFetchedAtDesc(SYMBOL);
    }

    @Test
    void findLastPriceNotFound() {
        when(repository.findFirstBySymbolOrderByFetchedAtDesc(SYMBOL)).thenReturn(Optional.empty());
        assertThrows(NoPriceException.class, () -> priceService.findLastPrice());
        verify(repository).findFirstBySymbolOrderByFetchedAtDesc(SYMBOL);
    }

    @Test
    void getPricePageSuccess() {
        int pageNumber = 0;
        int pageSize = 2;
        long totalElements = 2;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        Price price1 = new Price(SYMBOL, LOW_PRICE, FROM);
        Price price2 = new Price(SYMBOL, HIGH_PRICE, TO);
        Page<Price> pageFromDb = new PageImpl<>(List.of(price1, price2), pageRequest, totalElements);
        when(repository.findAllBySymbolAndFetchedAtBetweenOrderByFetchedAtDesc(SYMBOL, FROM, TO, pageRequest)).thenReturn(pageFromDb);
        PricePageResponse response = priceService.getPricePage(FROM, TO, pageNumber, pageSize);
        assertNotNull(response);
        assertEquals(pageNumber, response.page());
        assertEquals(pageSize, response.size());
        assertEquals(totalElements, response.totalElements());
        assertFalse(response.hasNext());
        assertEquals(pageSize, response.items().size());
        assertEquals(LOW_PRICE, response.items().get(0).price());
    }

    @Test
    void getPricePageEmpty() {
        int pageNumber = 0;
        int pageSize = 10;
        long totalElements = 0;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        Page<Price> emptyPage = new PageImpl<>(List.of(), pageRequest, totalElements);
        when(repository.findAllBySymbolAndFetchedAtBetweenOrderByFetchedAtDesc(SYMBOL, FROM, TO, pageRequest)).thenReturn(emptyPage);
        PricePageResponse response = priceService.getPricePage(FROM, TO, pageNumber, pageSize);
        assertNotNull(response);
        assertTrue(response.items().isEmpty());
        assertEquals(totalElements, response.totalElements());
        assertFalse(response.hasNext());
    }

    @Test
    void findAveragePriceSuccess() {
        when(repository.findAveragePrice(SYMBOL, FROM, TO)).thenReturn(Optional.of(PRICE));
        PriceAverageResponse response = priceService.findAveragePrice(FROM, TO);
        assertNotNull(response);
        assertEquals(PRICE, response.averagePrice());
        verify(repository).findAveragePrice(SYMBOL, FROM, TO);
    }

    @Test
    void findAveragePriceNotFound() {
        when(repository.findAveragePrice(SYMBOL, FROM, TO)).thenReturn(Optional.empty());
        assertThrows(NoPriceException.class, () -> priceService.findAveragePrice(FROM, TO));
        verify(repository).findAveragePrice(SYMBOL, FROM, TO);
    }

    @Test
    void findPriceTrendUpward() {
        Price startPrice = new Price(SYMBOL, LOW_PRICE, FROM);
        Price endPrice = new Price(SYMBOL, HIGH_PRICE, TO);
        when(repository.findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtAsc(SYMBOL, FROM, TO)).thenReturn(Optional.of(startPrice));
        when(repository.findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtDesc(SYMBOL, FROM, TO)).thenReturn(Optional.of(endPrice));
        PriceTrendResponse response = priceService.findPriceTrend(FROM, TO);
        assertNotNull(response);
        assertEquals(PriceTrendResponse.Trend.UPWARD, response.trend());
        assertEquals(SYMBOL, response.symbol());
        assertEquals(FROM, response.startDate());
        assertEquals(TO, response.endDate());
    }

    @Test
    void findPriceTrendDownward() {
        Price startPrice = new Price(SYMBOL, HIGH_PRICE, FROM);
        Price endPrice = new Price(SYMBOL, LOW_PRICE, TO);
        when(repository.findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtAsc(SYMBOL, FROM, TO)).thenReturn(Optional.of(startPrice));
        when(repository.findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtDesc(SYMBOL, FROM, TO)).thenReturn(Optional.of(endPrice));
        PriceTrendResponse response = priceService.findPriceTrend(FROM, TO);
        assertNotNull(response);
        assertEquals(PriceTrendResponse.Trend.DOWNWARD, response.trend());
    }

    @Test
    void findPriceTrendFlat() {
        Price startPrice = new Price(SYMBOL, PRICE, FROM);
        Price endPrice = new Price(SYMBOL, PRICE, TO);
        when(repository.findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtAsc(SYMBOL, FROM, TO)).thenReturn(Optional.of(startPrice));
        when(repository.findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtDesc(SYMBOL, FROM, TO)).thenReturn(Optional.of(endPrice));
        PriceTrendResponse response = priceService.findPriceTrend(FROM, TO);
        assertNotNull(response);
        assertEquals(PriceTrendResponse.Trend.FLAT, response.trend());
    }

    @Test
    void findPriceTrendStartNotFound() {
        when(repository.findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtAsc(SYMBOL, FROM, TO)).thenReturn(Optional.empty());
        assertThrows(NoPriceException.class, () -> priceService.findPriceTrend(FROM, TO));
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
