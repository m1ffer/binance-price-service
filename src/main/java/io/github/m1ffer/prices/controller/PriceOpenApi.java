package io.github.m1ffer.prices.controller;

import io.github.m1ffer.prices.dto.request.DateTimeRangeRequest;
import io.github.m1ffer.prices.dto.request.PaginationRequest;
import io.github.m1ffer.prices.dto.response.AveragePriceResponse;
import io.github.m1ffer.prices.dto.response.PricePageResponse;
import io.github.m1ffer.prices.dto.response.PriceResponse;
import io.github.m1ffer.prices.dto.response.PriceTrendResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

@Tag(name = "Prices", description = "История и аналитика цен Binance")
public interface PriceOpenApi {

    @Operation(
            summary = "Получить последнюю цену",
            description = "Возвращает актуальную цену торговой пары")
    @ApiResponse(
            responseCode = "200",
            description = "Последняя цена успешно получена",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PriceResponse.class),
                    examples = @ExampleObject(
                            name = "Актуальная цена",
                            value = """
                                    {
                                      "symbol": "BNBUSDT",
                                      "price": 698.14886085,
                                      "fetchedAt": "2026-11-02T18:35:00Z"
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Цены не найдены",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            name = "Цены отсутствуют",
                            value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Not Found",
                                      "status": 404,
                                      "detail": "No prices found for symbol 'BNBUSDT'",
                                      "instance": "/api/v1/prices/latest"
                                    }
                                    """
                    )
            )
    )
    PriceResponse getLatestPrice();

    @Operation(
            summary = "Рассчитать среднюю цену за период",
            description = "Вычисляет среднюю цену торговой пары за указанный период")
    @ApiResponse(
            responseCode = "200",
            description = "Средняя цена успешно рассчитана",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AveragePriceResponse.class),
                    examples = @ExampleObject(
                            name = "Средняя цена",
                            value = """
                                    {
                                      "averagePrice": 698.54129031
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Некорректные параметры запроса",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            name = "Некорректный диапазон дат",
                            value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Bad Request",
                                      "status": 400,
                                      "detail": "Validation failed",
                                      "instance": "/api/v1/prices/average",
                                      "errors": [
                                        "Parameter 'to' must not be before 'from'"
                                      ]
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Цены за указанный период не найдены",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            name = "Данные за период не найдены",
                            value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Not Found",
                                      "status": 404,
                                      "detail": "No prices found for symbol 'BNBUSDT' in range [2026-11-01T00:00:00Z, 2026-11-02T00:00:00Z]",
                                      "instance": "/api/v1/prices/average"
                                    }
                                    """
                    )
            )
    )
    AveragePriceResponse getAveragePrice(@ParameterObject DateTimeRangeRequest range);

    @Operation(
            summary = "Получить список цен с пагинацией",
            description = "Возвращает список цен торговой пары за указанный период с пагинацией")
    @ApiResponse(
            responseCode = "200",
            description = "Список цен успешно получен",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PricePageResponse.class),
                    examples = @ExampleObject(
                            name = "Страница котировок",
                            value = """
                                    {
                                      "items": [
                                        {
                                          "symbol": "BNBUSDT",
                                          "price": 698.48670780,
                                          "fetchedAt": "2026-11-02T18:37:32Z"
                                        },
                                        {
                                          "symbol": "BNBUSDT",
                                          "price": 698.29880874,
                                          "fetchedAt": "2026-11-02T18:37:01Z"
                                        }
                                      ],
                                      "page": 0,
                                      "size": 20,
                                      "totalElements": 2,
                                      "hasNext": false
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Некорректные параметры запроса",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            name = "Ошибка валидации параметров пагинации",
                            value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Bad Request",
                                      "status": 400,
                                      "detail": "Validation failed",
                                      "instance": "/api/v1/prices",
                                      "errors": [
                                        "Parameter 'size' must be less or equal than 100"
                                      ]
                                    }
                                    """
                    )
            )
    )
    PricePageResponse getPrices(
            @ParameterObject DateTimeRangeRequest range,
            @ParameterObject PaginationRequest pageRequest
    );

    @Operation(
            summary = "Рассчитать ценовой тренд за период",
            description = "Вычисляет ценовой тренд торговой пары за указанный период")
    @ApiResponse(
            responseCode = "200",
            description = "Ценовой тренд успешно рассчитан",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PriceTrendResponse.class),
                    examples = @ExampleObject(
                            name = "Пример тренда",
                            value = """
                                    {
                                      "symbol": "BNBUSDT",
                                      "startDate": "2026-11-01T00:00:00Z",
                                      "endDate": "2026-11-02T00:00:00Z",
                                      "trend": "UPWARD"
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Некорректные параметры запроса",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            name = "Некорректный диапазон дат",
                            value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Bad Request",
                                      "status": 400,
                                      "detail": "Validation failed",
                                      "instance": "/api/v1/prices/trend",
                                      "errors": [
                                        "Parameter 'to' must not be before 'from'"
                                      ]
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Цены за указанный период не найдены",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                            name = "Данные за период не найдены",
                            value = """
                                    {
                                      "type": "about:blank",
                                      "title": "Not Found",
                                      "status": 404,
                                      "detail": "No prices found for symbol 'BNBUSDT' in range [2026-11-01T00:00:00Z, 2026-11-02T00:00:00Z]",
                                      "instance": "/api/v1/prices/trend"
                                    }
                                    """
                    )
            )
    )
    PriceTrendResponse getPriceTrend(@ParameterObject DateTimeRangeRequest range);
}
