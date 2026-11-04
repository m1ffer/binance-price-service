package io.github.m1ffer.prices.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Binance Price Service API"));
    }

    @Bean
    public OperationCustomizer customizeQueryParams() {
        return (operation, handlerMethod) -> {
            if (operation.getParameters() != null) {
                for (var param : operation.getParameters()) {
                    switch (param.getName()) {
                        case "from" -> param.description("Начало периода в формате ISO-8601 UTC")
                                .example("2026-11-01T00:00:00Z");
                        case "to" -> param.description("Конец периода в формате ISO-8601 UTC")
                                .example("2026-11-02T00:00:00Z");
                        case "page" -> param.description("Номер страницы (0..N)")
                                .example(0);
                        case "size" -> param.description("Размер страницы (1..100)")
                                .example(20);
                    }
                }
            }
            return operation;
        };
    }
}
