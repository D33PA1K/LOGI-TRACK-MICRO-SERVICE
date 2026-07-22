package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.SupplierDTO;
import com.cognizant.logitrack.enums.SupplierStatus;
import java.util.List;

public interface SupplierService {
    SupplierDTO addSupplier(SupplierDTO dto);
    List<SupplierDTO> getAllSuppliers();
    SupplierDTO getById(Integer id);
    SupplierDTO updateStatus(Integer id, SupplierStatus status);
    void deleteSupplier(Integer id);
}
