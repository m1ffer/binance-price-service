package io.github.m1ffer.prices.repository;

import io.github.m1ffer.prices.model.Ping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PingRepository extends JpaRepository<Ping, Long> {
    Optional<Ping> findFirstByOrderByTimeDesc();
}
