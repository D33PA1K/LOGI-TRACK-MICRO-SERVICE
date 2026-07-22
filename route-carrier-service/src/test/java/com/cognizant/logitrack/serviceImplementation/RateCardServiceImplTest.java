package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.repository.RateCardRepository;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RateCardServiceImplTest {

    @Mock
    private RateCardRepository rateCardRepository;

    @InjectMocks
    private RateCardServiceImpl rateCardService;

    @Test
    void testgetById_NotFound() {
        when(rateCardRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> rateCardService.getById(1));
    }
}

