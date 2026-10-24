package io.github.m1ffer.prices.service;

import io.github.m1ffer.prices.dto.PingResponse;
import io.github.m1ffer.prices.exception.NoPingException;
import io.github.m1ffer.prices.model.Ping;
import io.github.m1ffer.prices.repository.PingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PingService {
    private final PingRepository repository;
    @Transactional
    public PingResponse addPing(long time){
        Ping newPing = new Ping(time);
        repository.save(newPing);
        return new PingResponse(newPing);
    }

    @Transactional(readOnly = true)
    public PingResponse getLastPing(){
        Ping founded = repository.findFirstByOrderByTimeDesc()
                .orElseThrow(NoPingException::new);
        return new PingResponse(founded);
    }
}
