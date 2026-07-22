package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.service.InventoryService;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.InventoryDTO;
import com.cognizant.logitrack.entity.WarehouseInventory;
import com.cognizant.logitrack.repository.WarehouseInventoryRepository;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InventoryServiceImpl implements InventoryService {
    private final WarehouseInventoryRepository inventoryRepository;

    public InventoryServiceImpl(WarehouseInventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public List<InventoryDTO> getInventoryByWarehouse(Integer warehouseId) {
        return inventoryRepository.findByWarehouseId(warehouseId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public InventoryDTO getById(Integer id) {
        return toDTO(findEntity(id));
    }

    @Override
    public InventoryDTO updateQuantity(Integer id, Integer quantity) {
        WarehouseInventory inv = findEntity(id);
        inv.setQuantityOnHand(quantity);
        return toDTO(inventoryRepository.save(inv));
    }

    @Override
    public InventoryDTO reserveStock(Integer id, Integer quantity) {
        WarehouseInventory inv = findEntity(id);
        if (inv.getQuantityOnHand() == null || inv.getQuantityOnHand() < quantity) {
            throw new BadRequestException("Insufficient stock");
        }
        inv.setQuantityOnHand(inv.getQuantityOnHand() - quantity);
        inv.setQuantityReserved((inv.getQuantityReserved() == null ? 0 : inv.getQuantityReserved()) + quantity);
        log.info("Reserved {} units of inventory {}", quantity, id);
        return toDTO(inventoryRepository.save(inv));
    }

    @Override
    public InventoryDTO releaseStock(Integer id, Integer quantity) {
        WarehouseInventory inv = findEntity(id);
        inv.setQuantityReserved((inv.getQuantityReserved() == null ? 0 : inv.getQuantityReserved()) - quantity);
        inv.setQuantityOnHand((inv.getQuantityOnHand() == null ? 0 : inv.getQuantityOnHand()) + quantity);
        log.info("Released {} units of inventory {}", quantity, id);
        return toDTO(inventoryRepository.save(inv));
    }

    @Override
    public InventoryDTO consumeStock(Integer id, Integer quantity) {
        WarehouseInventory inv = findEntity(id);
        if (inv.getQuantityReserved() == null || inv.getQuantityReserved() < quantity) {
            throw new BadRequestException("Insufficient reserved stock to consume");
        }
        inv.setQuantityReserved(inv.getQuantityReserved() - quantity);
        log.info("Consumed {} units of reserved inventory {}", quantity, id);
        return toDTO(inventoryRepository.save(inv));
    }

    private WarehouseInventory findEntity(Integer id) {
        return inventoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));
    }

    private InventoryDTO toDTO(WarehouseInventory inv) {
        return InventoryDTO.builder().inventoryId(inv.getInventoryId()).sku(inv.getSku()).productName(inv.getProductName()).warehouseId(inv.getWarehouseId()).binLocation(inv.getBinLocation()).quantityOnHand(inv.getQuantityOnHand()).quantityReserved(inv.getQuantityReserved()).lastUpdated(inv.getLastUpdated()).build();
    }
}
