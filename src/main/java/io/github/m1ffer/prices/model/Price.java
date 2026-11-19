package io.github.m1ffer.prices.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

@Entity
@Table(
        name = "prices",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_prices_symbol_fetched_at",
                columnNames = {"symbol", "fetched_at"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Price {
    public static final int SYMBOL_MAX_LENGTH = 20;
    public static final int PRICE_PRECISION = 18;
    public static final int PRICE_SCALE = 8;
    public static final TemporalUnit TIMESTAMP_PRECISION = ChronoUnit.MICROS;
    public static final Duration HALF_PRECISION = TIMESTAMP_PRECISION.getDuration().dividedBy(2);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Column(name = "symbol", nullable = false, length = SYMBOL_MAX_LENGTH)
    private String symbol;

    @Positive
    @Column(name = "price", nullable = false, precision = PRICE_PRECISION, scale = PRICE_SCALE)
    private BigDecimal price;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    public Price(String symbol, BigDecimal price, Instant fetchedAt) {
        this.symbol = symbol;
        this.price = roundPrice(price);
        this.fetchedAt = roundFetchedAt(fetchedAt);
    }

    //TODO подумать над корректностью этих проверок
    private static BigDecimal roundPrice(BigDecimal price) {
        return price.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }

    private static Instant roundFetchedAt(Instant fetchedAt) {
        return fetchedAt.plus(HALF_PRECISION)
                .truncatedTo(TIMESTAMP_PRECISION);
    }
}
