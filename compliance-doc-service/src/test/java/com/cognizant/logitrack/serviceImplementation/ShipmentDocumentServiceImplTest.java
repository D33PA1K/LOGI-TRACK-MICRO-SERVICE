package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.repository.ShipmentDocumentRepository;
import com.cognizant.logitrack.client.ShipmentClient;
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
public class ShipmentDocumentServiceImplTest {

    @Mock
    private ShipmentDocumentRepository documentRepository;
    @Mock
    private ShipmentClient shipmentClient;

    @InjectMocks
    private ShipmentDocumentServiceImpl documentService;

    @Test
    void testGetById_NotFound() {
        when(documentRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> documentService.getById(1));
    }
}
