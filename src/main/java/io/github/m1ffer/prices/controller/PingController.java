package io.github.m1ffer.prices.controller;

import io.github.m1ffer.prices.dto.PingResponse;
import io.github.m1ffer.prices.service.PingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PingController {
    private final PingService service;
    @GetMapping("/ping")
    public PingResponse ping(){
        long currTime = System.currentTimeMillis();
        return service.addPing(currTime);
    }
    @GetMapping("/last-ping")
    public PingResponse lastPing(){
        return service.getLastPing();
    }
}
