package io.github.m1ffer.prices.repository;

import io.github.m1ffer.prices.dto.response.PriceResponse;
import io.github.m1ffer.prices.model.Price;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {
    //last
    Optional<Price> findFirstBySymbolOrderByFetchedAtDesc(String symbol);

    //pag
    Page<Price> findAllBySymbolAndFetchedAtBetweenOrderByFetchedAtDesc(
            String symbol, Instant from, Instant to, Pageable page
    );

    //avg
    @Query("""
           SELECT CAST(AVG(p.price) AS java.math.BigDecimal)
           FROM Price p
           WHERE p.symbol = :symbol
             AND p.fetchedAt >= :from
             AND p.fetchedAt <= :to
           """)
    Optional<BigDecimal> findAveragePrice(
            @Param("symbol") String symbol,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    //trend
    Optional<Price> findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtAsc(
            String symbol, Instant from, Instant to);
    Optional<Price> findFirstBySymbolAndFetchedAtBetweenOrderByFetchedAtDesc(
            String symbol, Instant from, Instant to
    );
}
