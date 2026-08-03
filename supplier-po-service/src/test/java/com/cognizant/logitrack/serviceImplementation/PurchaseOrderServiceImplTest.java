package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.client.WarehouseClient;
import com.cognizant.logitrack.dto.PurchaseOrderDTO;
import com.cognizant.logitrack.entity.Supplier;
import com.cognizant.logitrack.enums.SupplierStatus;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.repository.PurchaseOrderRepository;
import com.cognizant.logitrack.repository.SupplierRepository;
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
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private WarehouseClient warehouseClient;

    @InjectMocks
    private PurchaseOrderServiceImpl purchaseOrderService;

    @Test
    void testgetById_NotFound() {
        when(purchaseOrderRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> purchaseOrderService.getById(1));
    }

    @Test
    void testCreatePO_InvalidWarehouse_Rejected() {
        Supplier supplier = Supplier.builder().supplierId(1).name("Acme").status(SupplierStatus.ACTIVE).build();
        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));
        when(warehouseClient.getWarehouseById(99))
                .thenThrow(new BadRequestException("Warehouse #99 does not exist."));

        PurchaseOrderDTO dto = PurchaseOrderDTO.builder().supplierId(1).warehouseId(99).build();

        assertThrows(BadRequestException.class, () -> purchaseOrderService.createPO(dto));
    }
}
