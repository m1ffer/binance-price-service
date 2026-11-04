package io.github.m1ffer.prices.client.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
@EnableConfigurationProperties(BinanceClientProperties.class)
public class BinanceRestClientConfiguration {
    @Bean
    public RestClient binanceRestClient(
            RestClient.Builder builder,
            BinanceClientProperties configuration
    ) {
        var httpClient = HttpClient.newBuilder()
                .version(configuration.httpVersion())
                .connectTimeout(configuration.connectTimeout())
                .build();
        var factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(configuration.readTimeout());
        return builder.requestFactory(factory)
                .baseUrl(configuration.baseUrl())
                .build();
    }
}
