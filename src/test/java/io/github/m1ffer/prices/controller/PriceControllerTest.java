package io.github.m1ffer.prices.controller;

import io.github.m1ffer.prices.dto.request.DateTimeRangeRequest;
import io.github.m1ffer.prices.dto.request.PaginationRequest;
import io.github.m1ffer.prices.dto.response.AveragePriceResponse;
import io.github.m1ffer.prices.dto.response.PricePageResponse;
import io.github.m1ffer.prices.dto.response.PriceResponse;
import io.github.m1ffer.prices.dto.response.PriceTrendResponse;
import io.github.m1ffer.prices.exception.PriceNotFoundException;
import io.github.m1ffer.prices.service.PriceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PriceController.class)
class PriceControllerTest {
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
    void getLatestPriceSuccess() throws Exception {
        when(priceService.findLatestPrice())
                .thenReturn(new PriceResponse(SYMBOL, PRICE, FETCHED_AT));

        mockMvc.perform(get("/api/v1/prices/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value(SYMBOL))
                .andExpect(jsonPath("$.price").value(PRICE));
    }

    @Test
    void getLatestPriceNotFound() throws Exception {
        when(priceService.findLatestPrice()).thenThrow(PriceNotFoundException.forSymbol(SYMBOL));

        mockMvc.perform(get("/api/v1/prices/latest"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getAveragePriceSuccess() throws Exception {
        when(priceService.calculateAveragePrice(FROM, TO))
                .thenReturn(new AveragePriceResponse(PRICE));

        mockMvc.perform(get("/api/v1/prices/average")
                        .param("from", FROM.toString())
                        .param("to", TO.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averagePrice").value(PRICE));
    }

    @Test
    void getPricesSuccess() throws Exception {
        PricePageResponse response =
                new PricePageResponse(Collections.emptyList(), 0, 10, 0, false);
        when(priceService.findPrices(FROM, TO, 0, 10)).thenReturn(response);

        mockMvc.perform(get("/api/v1/prices")
                        .param("from", FROM.toString())
                        .param("to", TO.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getPriceTrendSuccess() throws Exception {
        PriceTrendResponse response = new PriceTrendResponse(
                SYMBOL, FROM, TO, PriceTrendResponse.Trend.UPWARD);
        when(priceService.calculatePriceTrend(FROM, TO)).thenReturn(response);

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
    void validationMissingSingleParameter() throws Exception {
        mockMvc.perform(get("/api/v1/prices/average")
                        .param("from", FROM.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasItem("Parameter 'to' must not be null")));
    }

    @Test
    void validationInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/v1/prices/average")
                        .param("from", TO.toString())
                        .param("to", FROM.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasItem("Parameter 'to' must not be before 'from'")));
    }

    @Test
    void validationDateBeforeMinSupported() throws Exception {
        mockMvc.perform(get("/api/v1/prices/average")
                        .param("from", DateTimeRangeRequest.MIN_SUPPORTED_DATE.minusSeconds(1).toString())
                        .param("to", TO.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasItem(
                        "Parameter 'from' must be between "
                                + DateTimeRangeRequest.MIN_SUPPORTED_DATE_STRING
                                + " and "
                                + DateTimeRangeRequest.MAX_SUPPORTED_DATE_STRING)));
    }

    @Test
    void validationDateAfterMaxSupported() throws Exception {
        mockMvc.perform(get("/api/v1/prices/average")
                        .param("from", FROM.toString())
                        .param("to", DateTimeRangeRequest.MAX_SUPPORTED_DATE.plusSeconds(1).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasItem(
                        "Parameter 'to' must be between "
                                + DateTimeRangeRequest.MIN_SUPPORTED_DATE_STRING
                                + " and "
                                + DateTimeRangeRequest.MAX_SUPPORTED_DATE_STRING)));
    }

    @Test
    void validationMalformedDateFormat() throws Exception {
        mockMvc.perform(get("/api/v1/prices/average")
                        .param("from", "invalid-date")
                        .param("to", TO.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasItem(containsString("Failed to convert"))));
    }

    @Test
    void validationNegativePage() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("from", FROM.toString())
                        .param("to", TO.toString())
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasItem("Parameter 'page' must be positive or zero")));
    }

    @Test
    void validationZeroPageSize() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("from", FROM.toString())
                        .param("to", TO.toString())
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasItem("Parameter 'size' must be positive")));
    }

    @Test
    void validationNegativePageSize() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("from", FROM.toString())
                        .param("to", TO.toString())
                        .param("page", "0")
                        .param("size", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasItem("Parameter 'size' must be positive")));
    }

    @Test
    void validationPageSizeExceedsMax() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("from", FROM.toString())
                        .param("to", TO.toString())
                        .param("page", "0")
                        .param("size", String.valueOf(PaginationRequest.MAX_PAGE_SIZE + 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasItem(
                        "Parameter 'size' must be less or equal than " + PaginationRequest.MAX_PAGE_SIZE)));
    }

    @Test
    void validationMalformedPaginationParams() throws Exception {
        mockMvc.perform(get("/api/v1/prices")
                        .param("from", FROM.toString())
                        .param("to", TO.toString())
                        .param("page", "not-a-number")
                        .param("size", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasItem(containsString("Failed to convert"))));
    }

    @Test
    void validationTrendInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/v1/prices/trend")
                        .param("from", TO.toString())
                        .param("to", FROM.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasItem("Parameter 'to' must not be before 'from'")));
    }
}
