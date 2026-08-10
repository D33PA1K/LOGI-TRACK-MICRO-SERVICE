package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.client.NotificationClient;
import com.cognizant.logitrack.service.PickListService;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.NotificationDTO;
import com.cognizant.logitrack.dto.PickListDTO;
import com.cognizant.logitrack.entity.PickList;
import com.cognizant.logitrack.entity.WarehouseInventory;
import com.cognizant.logitrack.enums.NotificationCategory;
import com.cognizant.logitrack.enums.PickListStatus;
import com.cognizant.logitrack.repository.PickListRepository;
import com.cognizant.logitrack.repository.WarehouseInventoryRepository;
import com.cognizant.logitrack.repository.WarehouseRepository;
import com.cognizant.logitrack.client.FreightOrderClient;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class PickListServiceImpl implements PickListService {

    private final PickListRepository pickListRepository;
    private final NotificationClient notificationClient;
    private final FreightOrderClient freightOrderClient;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseInventoryRepository warehouseInventoryRepository;
    private final InventoryService inventoryService;

    public PickListServiceImpl(PickListRepository pickListRepository, NotificationClient notificationClient, FreightOrderClient freightOrderClient, WarehouseRepository warehouseRepository, WarehouseInventoryRepository warehouseInventoryRepository, InventoryService inventoryService) {
        this.pickListRepository = pickListRepository;
        this.notificationClient = notificationClient;
        this.freightOrderClient = freightOrderClient;
        this.warehouseRepository = warehouseRepository;
        this.warehouseInventoryRepository = warehouseInventoryRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    public PickListDTO createPickList(PickListDTO dto) {
        // A downstream failure surfaces its own message via the Feign fallback
        // (503 "Shipment/freight service unavailable" vs 400 "Freight order #x not found").
        Object order = freightOrderClient.getFreightOrderById(dto.getFreightOrderId());
        if (order == null) {
            throw new BadRequestException("Freight order does not exist: " + dto.getFreightOrderId());
        }

        if (!warehouseRepository.existsById(dto.getWarehouseId())) {
            throw new BadRequestException("Warehouse does not exist: " + dto.getWarehouseId());
        }

        // Reserve the stock BEFORE creating the pick list: if there is not enough
        // on hand we must fail outright rather than leave an unpickable pick list
        // behind that would then block dispatch.
        if (hasStockLine(dto.getSku(), dto.getQuantity())) {
            WarehouseInventory inventory = findInventory(dto.getSku(), dto.getWarehouseId());
            inventoryService.reserveStock(inventory.getInventoryId(), dto.getQuantity());
        }

        PickList pickList = PickList.builder()
                .freightOrderId(dto.getFreightOrderId())
                .warehouseId(dto.getWarehouseId())
                .assignedTo(dto.getAssignedTo())
                .sku(dto.getSku())
                .quantity(dto.getQuantity())
                .status(PickListStatus.OPEN)
                .build();
        PickList saved = pickListRepository.save(pickList);

        if (saved.getAssignedTo() != null) {
            sendNotification(saved.getAssignedTo(), "Pick list assigned to you", NotificationCategory.WAREHOUSE);
        }

        return toDTO(saved);
    }

    @Override
    public PickListDTO assignPickList(Integer id, Integer assignedTo) {
        PickList pickList = findEntity(id);
        pickList.setAssignedTo(assignedTo);
        pickList.setStatus(PickListStatus.INPROGRESS);
        PickList saved = pickListRepository.save(pickList);

        sendNotification(assignedTo, "Pick list assigned to you", NotificationCategory.WAREHOUSE);

        return toDTO(saved);
    }

    /**
     * Settles the stock reservation as the pick list reaches a terminal state:
     *  - COMPLETED: the goods have been picked and left the bin, so the reserved
     *    quantity is consumed (retired, not returned to on-hand).
     *  - CANCELLED / SHORTAGE: the pick will not happen, so the reservation is
     *    released back to on-hand and becomes available again.
     * Both are idempotent via the previous-status check, so re-applying the same
     * status cannot double-consume or double-release.
     */
    @Override
    public PickListDTO updatePickListStatus(Integer id, PickListStatus status) {
        PickList pickList = findEntity(id);
        PickListStatus previous = pickList.getStatus();

        if (previous != status && hasStockLine(pickList.getSku(), pickList.getQuantity())
                && !isTerminal(previous)) {
            if (status == PickListStatus.COMPLETED) {
                settleStock(pickList, true);
            } else if (status == PickListStatus.CANCELLED || status == PickListStatus.SHORTAGE) {
                settleStock(pickList, false);
            }
        }

        pickList.setStatus(status);
        PickList saved = pickListRepository.save(pickList);

        sendNotification(saved.getAssignedTo(),
                "Pick list #" + id + " is now " + status,
                NotificationCategory.WAREHOUSE);

        return toDTO(saved);
    }

    private static boolean isTerminal(PickListStatus status) {
        return status == PickListStatus.COMPLETED
                || status == PickListStatus.CANCELLED
                || status == PickListStatus.SHORTAGE;
    }

    private static boolean hasStockLine(String sku, Integer quantity) {
        return sku != null && !sku.isBlank() && quantity != null && quantity > 0;
    }

    private void settleStock(PickList pickList, boolean consume) {
        WarehouseInventory inventory = findInventory(pickList.getSku(), pickList.getWarehouseId());

        if (consume) {
            inventoryService.consumeStock(inventory.getInventoryId(), pickList.getQuantity());
        } else {
            inventoryService.releaseStock(inventory.getInventoryId(), pickList.getQuantity());
        }
    }

    private WarehouseInventory findInventory(String sku, Integer warehouseId) {
        return warehouseInventoryRepository.findBySku(sku).stream()
                .filter(inventory -> inventory.getWarehouseId() != null
                        && inventory.getWarehouseId().equals(warehouseId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("SKU " + sku
                        + " is not stocked in warehouse " + warehouseId + "."));
    }

    @Override
    public List<PickListDTO> getByWarehouse(Integer warehouseId) {
        return pickListRepository.findByWarehouseId(warehouseId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PickListDTO> getAllPickLists() {
        return pickListRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PickListDTO> getByAssignedUser(Integer userId) {
        return pickListRepository.findByAssignedTo(userId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Used by shipment-freight-service (via PickListClient) to check the
     * "pick list must be COMPLETED" gate before a shipment can be dispatched.
     * Returns an empty list when the freight order has no pick lists yet —
     * that is a legitimate "gate not satisfied" answer, not an error.
     */
    @Override
    public List<PickListDTO> getByFreightOrder(Integer freightOrderId) {
        return pickListRepository.findByFreightOrderId(freightOrderId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    private PickList findEntity(Integer id) {
        return pickListRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pick list not found with id: " + id));
    }

    private void sendNotification(Integer userId, String message, NotificationCategory category) {
        if (userId == null) return;
        try {
            notificationClient.sendNotification(NotificationDTO.builder().userId(userId).message(message).category(category).build());
        } catch (Exception e) {}
    }

    private PickListDTO toDTO(PickList p) {
        return PickListDTO.builder().pickListId(p.getPickListId()).freightOrderId(p.getFreightOrderId()).warehouseId(p.getWarehouseId()).assignedTo(p.getAssignedTo()).sku(p.getSku()).quantity(p.getQuantity()).status(p.getStatus()).createdDate(p.getCreatedDate()).build();
    }
}

