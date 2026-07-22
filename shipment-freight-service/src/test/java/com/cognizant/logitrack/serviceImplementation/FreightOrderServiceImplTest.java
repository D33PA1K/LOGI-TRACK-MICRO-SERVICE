package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.repository.FreightOrderRepository;
import com.cognizant.logitrack.client.RouteClient;
import com.cognizant.logitrack.client.IdentityClient;
import com.cognizant.logitrack.client.PurchaseOrderClient;
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
public class FreightOrderServiceImplTest {

    @Mock
    private FreightOrderRepository freightOrderRepository;
    @Mock
    private RouteClient routeClient;
    @Mock
    private IdentityClient identityClient;
    @Mock
    private PurchaseOrderClient purchaseOrderClient;

    @InjectMocks
    private FreightOrderServiceImpl freightOrderService;

    @Test
    void testGetById_NotFound() {
        when(freightOrderRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> freightOrderService.getById(1));
    }
}
