package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.service.WarehouseService;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.WarehouseDTO;
import com.cognizant.logitrack.entity.Warehouse;
import com.cognizant.logitrack.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WarehouseServiceImpl implements WarehouseService {
    private final WarehouseRepository warehouseRepository;

    public WarehouseServiceImpl(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public List<WarehouseDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public WarehouseDTO getById(Integer id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
        return toDTO(warehouse);
    }

    private WarehouseDTO toDTO(Warehouse w) {
        return WarehouseDTO.builder()
                .warehouseId(w.getWarehouseId())
                .warehouseName(w.getWarehouseName())
                .addressLine(w.getAddressLine())
                .city(w.getCity())
                .state(w.getState())
                .country(w.getCountry())
                .postalCode(w.getPostalCode())
                .contactNumber(w.getContactNumber())
                .status(w.getStatus())
                .build();
    }
}
