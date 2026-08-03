package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.client.NotificationClient;
import com.cognizant.logitrack.service.PickListService;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.NotificationDTO;
import com.cognizant.logitrack.dto.PickListDTO;
import com.cognizant.logitrack.entity.PickList;
import com.cognizant.logitrack.enums.NotificationCategory;
import com.cognizant.logitrack.enums.PickListStatus;
import com.cognizant.logitrack.repository.PickListRepository;
import com.cognizant.logitrack.repository.WarehouseRepository;
import com.cognizant.logitrack.client.FreightOrderClient;
import com.cognizant.logitrack.exception.BadRequestException;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PickListServiceImpl implements PickListService {

    private final PickListRepository pickListRepository;
    private final NotificationClient notificationClient;
    private final FreightOrderClient freightOrderClient;
    private final WarehouseRepository warehouseRepository;

    public PickListServiceImpl(PickListRepository pickListRepository, NotificationClient notificationClient, FreightOrderClient freightOrderClient, WarehouseRepository warehouseRepository) {
        this.pickListRepository = pickListRepository;
        this.notificationClient = notificationClient;
        this.freightOrderClient = freightOrderClient;
        this.warehouseRepository = warehouseRepository;
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

        PickList pickList = PickList.builder()
                .freightOrderId(dto.getFreightOrderId())
                .warehouseId(dto.getWarehouseId())
                .assignedTo(dto.getAssignedTo())
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

    @Override
    public PickListDTO updatePickListStatus(Integer id, PickListStatus status) {
        PickList pickList = findEntity(id);
        pickList.setStatus(status);
        PickList saved = pickListRepository.save(pickList);

        sendNotification(saved.getAssignedTo(),
                "Pick list #" + id + " is now " + status,
                NotificationCategory.WAREHOUSE);

        return toDTO(saved);
    }

    @Override
    public List<PickListDTO> getByWarehouse(Integer warehouseId) {
        return pickListRepository.findByWarehouseId(warehouseId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PickListDTO> getByAssignedUser(Integer userId) {
        return pickListRepository.findByAssignedTo(userId).stream().map(this::toDTO).collect(Collectors.toList());
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
        return PickListDTO.builder().pickListId(p.getPickListId()).freightOrderId(p.getFreightOrderId()).warehouseId(p.getWarehouseId()).assignedTo(p.getAssignedTo()).status(p.getStatus()).createdDate(p.getCreatedDate()).build();
    }
}

