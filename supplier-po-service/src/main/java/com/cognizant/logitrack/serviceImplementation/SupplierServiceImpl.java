package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.service.SupplierService;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.SupplierDTO;
import com.cognizant.logitrack.entity.Supplier;
import com.cognizant.logitrack.enums.SupplierStatus;
import com.cognizant.logitrack.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepository;

    public SupplierServiceImpl(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Override
    public SupplierDTO addSupplier(SupplierDTO dto) {
        Supplier supplier = Supplier.builder().name(dto.getName()).category(dto.getCategory()).contactDetails(dto.getContactDetails()).leadTimeDays(dto.getLeadTimeDays()).status(SupplierStatus.ACTIVE).build();
        Supplier saved = supplierRepository.save(supplier);
        log.info("Supplier added: id={}, name={}", saved.getSupplierId(), saved.getName());
        return toDTO(saved);
    }

    @Override
    public List<SupplierDTO> getAllSuppliers() {
        return supplierRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public SupplierDTO getById(Integer id) {
        return toDTO(findEntity(id));
    }

    @Override
    public SupplierDTO updateStatus(Integer id, SupplierStatus status) {
        Supplier supplier = findEntity(id);
        supplier.setStatus(status);
        return toDTO(supplierRepository.save(supplier));
    }

    @Override
    public void deleteSupplier(Integer id) {
        Supplier supplier = findEntity(id);
        supplier.setStatus(SupplierStatus.INACTIVE);
        supplierRepository.save(supplier);
        log.info("Supplier soft-deleted: id={}", id);
    }

    private Supplier findEntity(Integer id) {
        return supplierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    private SupplierDTO toDTO(Supplier s) {
        return SupplierDTO.builder().supplierId(s.getSupplierId()).name(s.getName()).category(s.getCategory()).contactDetails(s.getContactDetails()).leadTimeDays(s.getLeadTimeDays()).status(s.getStatus()).build();
    }
}
