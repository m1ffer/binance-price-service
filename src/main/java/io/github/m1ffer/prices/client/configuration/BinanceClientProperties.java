package io.github.m1ffer.prices.client.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.http.HttpClient;
import java.time.Duration;

@ConfigurationProperties(prefix = "binance")
public record BinanceClientProperties(
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout,
        HttpClient.Version httpVersion
) {
}
