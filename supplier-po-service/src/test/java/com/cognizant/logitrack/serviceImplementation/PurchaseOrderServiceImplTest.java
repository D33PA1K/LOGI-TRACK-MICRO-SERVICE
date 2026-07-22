package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PurchaseOrderServiceImplTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @InjectMocks
    private PurchaseOrderServiceImpl purchaseOrderService;

    @Test
    void testgetById_NotFound() {
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> purchaseOrderService.getById(1));
    }
}

