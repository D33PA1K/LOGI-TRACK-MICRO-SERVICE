package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.dto.SupplierDTO;
import com.cognizant.logitrack.entity.Supplier;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    @Test
    void testgetById_NotFound() {
        when(supplierRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> supplierService.getById(1));
    }

    @Test
    void testAddSupplier_Duplicate_Rejected() {
        Supplier existing = Supplier.builder().supplierId(1).name("Acme Electronic").category("Electronics").contactDetails("acme@suppliers.com").build();
        when(supplierRepository.findByNameIgnoreCase("acme electronic")).thenReturn(List.of(existing));

        SupplierDTO dto = SupplierDTO.builder().name("acme electronic").category("ELECTRONICS").contactDetails("acme@suppliers.com").build();

        assertThrows(BadRequestException.class, () -> supplierService.addSupplier(dto));
    }
}

