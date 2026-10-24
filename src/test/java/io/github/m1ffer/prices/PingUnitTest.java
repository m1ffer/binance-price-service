package io.github.m1ffer.prices;

import io.github.m1ffer.prices.exception.NoPingException;
import io.github.m1ffer.prices.repository.PingRepository;
import io.github.m1ffer.prices.service.PingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PingUnitTest {
    @InjectMocks
    private PingService service;
    @Mock
    private PingRepository repository;

    @Test
    public void testThrow(){
        when(repository.findFirstByOrderByTimeDesc())
                .thenAnswer(_ -> Optional.empty());
        assertThatThrownBy(() -> service.getLastPing())
                .isInstanceOf(NoPingException.class);
    }
}
