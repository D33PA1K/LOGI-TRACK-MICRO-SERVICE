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
    public List<InventoryDTO> getAllInventory() {
        return inventoryRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
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
        if (quantity == null || quantity < 0) {
            throw new BadRequestException("Quantity on hand cannot be negative.");
        }

        WarehouseInventory inv = findEntity(id);
        inv.setQuantityOnHand(quantity);
        return toDTO(inventoryRepository.save(inv));
    }

    /**
     * Moves stock from on-hand into reserved. Reserved stock is still physically
     * in the bin but is committed to a pick list, so it must not be counted as
     * available for anything else.
     */
    @Override
    public InventoryDTO reserveStock(Integer id, Integer quantity) {
        requirePositive(quantity, "reserve");

        WarehouseInventory inv = findEntity(id);
        int onHand = nullToZero(inv.getQuantityOnHand());

        if (onHand < quantity) {
            throw new BadRequestException("Insufficient stock for SKU " + inv.getSku()
                    + ": " + onHand + " on hand, " + quantity + " requested.");
        }

        inv.setQuantityOnHand(onHand - quantity);
        inv.setQuantityReserved(nullToZero(inv.getQuantityReserved()) + quantity);
        log.info("Reserved {} units of inventory {}", quantity, id);
        return toDTO(inventoryRepository.save(inv));
    }

    /** Returns reserved stock to on-hand, e.g. when a pick list is cancelled. */
    @Override
    public InventoryDTO releaseStock(Integer id, Integer quantity) {
        requirePositive(quantity, "release");

        WarehouseInventory inv = findEntity(id);
        int reserved = nullToZero(inv.getQuantityReserved());

        // Guard added: releasing more than is reserved previously drove
        // quantityReserved negative and invented stock out of nothing.
        if (reserved < quantity) {
            throw new BadRequestException("Cannot release " + quantity + " units: only "
                    + reserved + " are reserved for SKU " + inv.getSku() + ".");
        }

        inv.setQuantityReserved(reserved - quantity);
        inv.setQuantityOnHand(nullToZero(inv.getQuantityOnHand()) + quantity);
        log.info("Released {} units of inventory {}", quantity, id);
        return toDTO(inventoryRepository.save(inv));
    }

    /**
     * Consumes reserved stock — the goods have physically left the warehouse, so
     * the reservation is retired without returning anything to on-hand.
     */
    @Override
    public InventoryDTO consumeStock(Integer id, Integer quantity) {
        requirePositive(quantity, "consume");

        WarehouseInventory inv = findEntity(id);
        int reserved = nullToZero(inv.getQuantityReserved());

        if (reserved < quantity) {
            throw new BadRequestException("Insufficient reserved stock to consume for SKU "
                    + inv.getSku() + ": " + reserved + " reserved, " + quantity + " requested.");
        }

        inv.setQuantityReserved(reserved - quantity);
        log.info("Consumed {} units of reserved inventory {}", quantity, id);
        return toDTO(inventoryRepository.save(inv));
    }

    private static int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private static void requirePositive(Integer quantity, String action) {
        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("Quantity to " + action + " must be greater than zero.");
        }
    }

    private WarehouseInventory findEntity(Integer id) {
        return inventoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));
    }

    private InventoryDTO toDTO(WarehouseInventory inv) {
        return InventoryDTO.builder().inventoryId(inv.getInventoryId()).sku(inv.getSku()).productName(inv.getProductName()).warehouseId(inv.getWarehouseId()).binLocation(inv.getBinLocation()).quantityOnHand(inv.getQuantityOnHand()).quantityReserved(inv.getQuantityReserved()).lastUpdated(inv.getLastUpdated()).build();
    }
}
