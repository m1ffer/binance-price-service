package io.github.m1ffer.prices.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "prices",
        uniqueConstraints = @UniqueConstraint(name = "uq_prices_symbol_fetched_at", columnNames = {"symbol", "fetched_at"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Positive
    @Column(name = "price", nullable = false, precision = 18, scale = 8)
    private BigDecimal price;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    @Override
    public boolean equals(Object o){
        if (o == this)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        Price other = (Price) o;
        return getId() != null && Objects.equals(getId(), other.getId());
    }

    @Override
    public int hashCode(){
        return Hibernate.getClass(this).hashCode();
    }

    public Price(String symbol, BigDecimal price, Instant fetchedAt) {
        Objects.requireNonNull(symbol, "symbol must not be null");
        if (symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        if (symbol.length() > 20) {
            throw new IllegalArgumentException("symbol length must not exceed 20 characters");
        }
        Objects.requireNonNull(price, "price must not be null");
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("price must be positive");
        }
        Objects.requireNonNull(fetchedAt, "fetchedAt must not be null");
        this.symbol = symbol;
        this.price = price;
        this.fetchedAt = fetchedAt;
    }
}
