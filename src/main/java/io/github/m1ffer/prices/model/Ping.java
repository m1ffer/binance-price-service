package io.github.m1ffer.prices.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ping")
@NoArgsConstructor
@Getter
@Setter
public class Ping{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "time", nullable = false)
    long time;

    public Ping(long time){
        this.time = time;
    }
}
