package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.WarehouseDTO;
import java.util.List;

public interface WarehouseService {
    List<WarehouseDTO> getAllWarehouses();
    WarehouseDTO getById(Integer id);
}
