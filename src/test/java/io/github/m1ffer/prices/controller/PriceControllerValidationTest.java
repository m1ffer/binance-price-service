package io.github.m1ffer.prices.controller;

import io.github.m1ffer.prices.dto.response.ListPriceResponse;
import io.github.m1ffer.prices.exception.GlobalExceptionHandler;
import io.github.m1ffer.prices.exception.ValidationExceptionHandler;
import io.github.m1ffer.prices.service.PriceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PriceController.class)
@Import({ValidationExceptionHandler.class, GlobalExceptionHandler.class})
class PriceControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PriceService priceService;

    @Nested
    @DisplayName("GET /api/v1/prices - Bad Request Scenarios")
    class GetPricesValidationTests {

        @Test
        @DisplayName("Should return 400 when 'symbol' parameter is missing")
        void shouldReturn400WhenSymbolIsMissing() throws Exception {
            mockMvc.perform(get("/api/v1/prices"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.title").value("Bad Request"))
                    .andExpect(jsonPath("$.detail").value("Validation failed"))
                    .andExpect(jsonPath("$.errors", hasItem("Parameter 'symbol' must be not empty")));
        }

        @Test
        @DisplayName("Should return 400 when 'symbol' parameter is empty string")
        void shouldReturn400WhenSymbolIsEmpty() throws Exception {
            mockMvc.perform(get("/api/v1/prices").param("symbol", ""))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasItem("Parameter 'symbol' must be not empty")));
        }

        @Test
        @DisplayName("Should return 400 when 'size' is zero")
        void shouldReturn400WhenSizeIsZero() throws Exception {
            mockMvc.perform(get("/api/v1/prices")
                            .param("symbol", "BTCUSDT")
                            .param("size", "0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasItem(containsString("Parameter 'size'"))))
                    .andExpect(jsonPath("$.errors", hasItem(containsString("greater than 0"))));
        }

        @Test
        @DisplayName("Should return 400 when 'size' is negative")
        void shouldReturn400WhenSizeIsNegative() throws Exception {
            mockMvc.perform(get("/api/v1/prices")
                            .param("symbol", "BTCUSDT")
                            .param("size", "-5"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasItem(containsString("Parameter 'size'"))))
                    .andExpect(jsonPath("$.errors", hasItem(containsString("greater than 0"))));
        }

        @Test
        @DisplayName("Should return 400 when 'size' exceeds maximum of 100")
        void shouldReturn400WhenSizeExceedsMax() throws Exception {
            mockMvc.perform(get("/api/v1/prices")
                            .param("symbol", "BTCUSDT")
                            .param("size", "101"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasItem(containsString("Parameter 'size'"))))
                    .andExpect(jsonPath("$.errors", hasItem(containsString("less than or equal to 100"))));
        }

        @Test
        @DisplayName("Should return 400 when 'size' is not a number")
        void shouldReturn400WhenSizeIsNotANumber() throws Exception {
            mockMvc.perform(get("/api/v1/prices")
                            .param("symbol", "BTCUSDT")
                            .param("size", "invalid_size"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasItem("Parameter 'size' must be of type Integer (got 'invalid_size')")));
        }

        @Test
        @DisplayName("Should return 400 when 'direction' is invalid enum value")
        void shouldReturn400WhenDirectionIsInvalid() throws Exception {
            mockMvc.perform(get("/api/v1/prices")
                            .param("symbol", "BTCUSDT")
                            .param("direction", "SIDEWAYS"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasItem("Parameter 'direction' has invalid value 'SIDEWAYS'. Expected one of: [ASC, DESC]")));
        }

        @Test
        @DisplayName("Should return 400 when 'to' date is before 'from' date")
        void shouldReturn400WhenToIsBeforeFrom() throws Exception {
            mockMvc.perform(get("/api/v1/prices")
                            .param("symbol", "BTCUSDT")
                            .param("from", "2026-06-15T00:00:00Z")
                            .param("to", "2026-06-01T00:00:00Z"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasItem("Parameter 'to' must not be before 'from'")));
        }

        @Test
        @DisplayName("Should return 400 when 'from' is before minimum supported date (1970)")
        void shouldReturn400WhenFromIsBeforeMin() throws Exception {
            mockMvc.perform(get("/api/v1/prices")
                            .param("symbol", "BTCUSDT")
                            .param("from", "1950-01-01T00:00:00Z"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasItem(containsString("Parameter 'from' must be between"))));
        }

        @Test
        @DisplayName("Should return 400 when 'from' has invalid date/time format")
        void shouldReturn400WhenFromHasInvalidDateFormat() throws Exception {
            mockMvc.perform(get("/api/v1/prices")
                            .param("symbol", "BTCUSDT")
                            .param("from", "not-a-date"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasItem("Parameter 'from' must be a valid date in ISO-8601 format (got 'not-a-date')")));
        }

        @Test
        @DisplayName("Should return 400 when 'to' has invalid date/time format")
        void shouldReturn400WhenToHasInvalidDateFormat() throws Exception {
            mockMvc.perform(get("/api/v1/prices")
                            .param("symbol", "BTCUSDT")
                            .param("to", "gibberish"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasItem("Parameter 'to' must be a valid date in ISO-8601 format (got 'gibberish')")));
        }

        @Test
        @DisplayName("Should aggregate multiple validation errors into the errors array")
        void shouldAggregateMultipleValidationErrors() throws Exception {
            mockMvc.perform(get("/api/v1/prices")
                            .param("symbol", "")
                            .param("size", "-10"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasSize(2)))
                    .andExpect(jsonPath("$.errors", hasItem("Parameter 'symbol' must be not empty")))
                    .andExpect(jsonPath("$.errors", hasItem(containsString("Parameter 'size'"))));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/prices/statistics - Bad Request Scenarios")
    class GetStatisticsValidationTests {

        @Test
        @DisplayName("Should return 400 when 'symbol' is missing")
        void shouldReturn400WhenSymbolIsMissing() throws Exception {
            mockMvc.perform(get("/api/v1/prices/statistics"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasItem("Parameter 'symbol' must be not empty")));
        }

        @Test
        @DisplayName("Should return 400 when 'symbol' is empty")
        void shouldReturn400WhenSymbolIsEmpty() throws Exception {
            mockMvc.perform(get("/api/v1/prices/statistics").param("symbol", ""))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasItem("Parameter 'symbol' must be not empty")));
        }

        @Test
        @DisplayName("Should return 400 when date range is inverted")
        void shouldReturn400WhenDateRangeIsInverted() throws Exception {
            mockMvc.perform(get("/api/v1/prices/statistics")
                            .param("symbol", "BTCUSDT")
                            .param("from", "2026-08-20T00:00:00Z")
                            .param("to", "2026-08-10T00:00:00Z"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasItem("Parameter 'to' must not be before 'from'")));
        }

        @Test
        @DisplayName("Should return 400 when 'from' is invalid date")
        void shouldReturn400WhenFromIsInvalidDate() throws Exception {
            mockMvc.perform(get("/api/v1/prices/statistics")
                            .param("symbol", "BTCUSDT")
                            .param("from", "bad-date"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasItem("Parameter 'from' must be a valid date in ISO-8601 format (got 'bad-date')")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/prices/trend - Bad Request Scenarios")
    class GetTrendValidationTests {

        @Test
        @DisplayName("Should return 400 when 'symbol' is missing")
        void shouldReturn400WhenSymbolIsMissing() throws Exception {
            mockMvc.perform(get("/api/v1/prices/trend"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasItem("Parameter 'symbol' must be not empty")));
        }

        @Test
        @DisplayName("Should return 400 when date range is inverted")
        void shouldReturn400WhenDateRangeIsInverted() throws Exception {
            mockMvc.perform(get("/api/v1/prices/trend")
                            .param("symbol", "ETHUSDT")
                            .param("from", "2026-11-15T00:00:00Z")
                            .param("to", "2026-11-01T00:00:00Z"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors", hasItem("Parameter 'to' must not be before 'from'")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/prices/latest - Scenarios")
    class LatestPriceValidationTests {

        @Test
        @DisplayName("Should return 400 when symbols list contains an empty element")
        void shouldReturn400WhenSymbolsContainsEmptyElement() throws Exception {
            mockMvc.perform(get("/api/v1/prices/latest")
                            .param("symbols", "BTCUSDT", ""))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors", hasItem("Parameter 'symbol' must be not empty")));
        }

        @Test
        @DisplayName("Should return 200 when symbols parameter is empty (symbols=)")
        void shouldReturn200WhenSymbolsParameterIsEmpty() throws Exception {
            when(priceService.getLatestPrice(any())).thenReturn(new ListPriceResponse(List.of()));

            mockMvc.perform(get("/api/v1/prices/latest").param("symbols", ""))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 200 when symbols parameter is omitted entirely")
        void shouldReturn200WhenSymbolsParameterIsOmitted() throws Exception {
            when(priceService.getLatestPrice(any())).thenReturn(new ListPriceResponse(List.of()));

            mockMvc.perform(get("/api/v1/prices/latest"))
                    .andExpect(status().isOk());
        }
    }
}
