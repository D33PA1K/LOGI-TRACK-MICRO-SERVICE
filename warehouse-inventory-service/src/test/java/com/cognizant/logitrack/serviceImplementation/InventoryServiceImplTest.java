package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.repository.WarehouseInventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceImplTest {

    @Mock
    private WarehouseInventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void testGetInventoryByWarehouse() {
        when(inventoryRepository.findByWarehouseId(1)).thenReturn(Collections.emptyList());
        assertNotNull(inventoryService.getInventoryByWarehouse(1));
    }
}
