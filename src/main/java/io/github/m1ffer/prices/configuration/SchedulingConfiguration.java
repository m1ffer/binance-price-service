package io.github.m1ffer.prices.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnBooleanProperty(
        name = "scheduler.enabled",
        matchIfMissing = true
)
public class SchedulingConfiguration {
}
