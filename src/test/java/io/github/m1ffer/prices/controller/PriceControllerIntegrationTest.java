package io.github.m1ffer.prices.controller;

import io.github.m1ffer.prices.dto.response.PriceAverageResponse;
import io.github.m1ffer.prices.dto.response.PricePageResponse;
import io.github.m1ffer.prices.dto.response.PriceResponse;
import io.github.m1ffer.prices.dto.response.PriceTrendResponse;
import io.github.m1ffer.prices.exception.NoPriceException;
import io.github.m1ffer.prices.service.PriceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PriceController.class)
class PriceControllerIntegrationTest {
    private static final String SYMBOL = "Some symbol";
    private static final BigDecimal PRICE = BigDecimal.ONE;
    private static final Instant FROM = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-01-01T12:00:00Z");
    private static final Instant FETCHED_AT = FROM;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PriceService priceService;

    @Test
    void findLastPriceSuccess() throws Exception {
        when(priceService.findLastPrice()).thenReturn(new PriceResponse(SYMBOL, PRICE, FETCHED_AT));

        mockMvc.perform(get("/api/v1/prices/last"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value(SYMBOL))
                .andExpect(jsonPath("$.price").value(PRICE));
    }

    @Test
    void findLastPriceNotFound() throws Exception {
        when(priceService.findLastPrice()).thenThrow(NoPriceException.forSymbol(SYMBOL));

        mockMvc.perform(get("/api/v1/prices/last"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void findAveragePriceSuccess() throws Exception {
        when(priceService.findAveragePrice(FROM, TO)).thenReturn(new PriceAverageResponse(PRICE));

        mockMvc.perform(get("/api/v1/prices/average")
                        .param("from", FROM.toString())
                        .param("to", TO.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averagePrice").value(PRICE));
    }

    @Test
    void getPricePageSuccess() throws Exception {
        PricePageResponse response = new PricePageResponse(Collections.emptyList(), 0, 10, 0, false);
        when(priceService.getPricePage(FROM, TO, 0, 10)).thenReturn(response);

        mockMvc.perform(get("/api/v1/prices")
                        .param("from", FROM.toString())
                        .param("to", TO.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void findPriceTrendSuccess() throws Exception {
        PriceTrendResponse response = new PriceTrendResponse(SYMBOL, FROM, TO, PriceTrendResponse.Trend.UPWARD);
        when(priceService.findPriceTrend(FROM, TO)).thenReturn(response);

        mockMvc.perform(get("/api/v1/prices/trend")
                        .param("from", FROM.toString())
                        .param("to", TO.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trend").value("UPWARD"));
    }

    @Test
    void validationMissingParameters() throws Exception {
        mockMvc.perform(get("/api/v1/prices"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void validationInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/v1/prices/average")
                        .param("from", TO.toString())
                        .param("to", FROM.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
