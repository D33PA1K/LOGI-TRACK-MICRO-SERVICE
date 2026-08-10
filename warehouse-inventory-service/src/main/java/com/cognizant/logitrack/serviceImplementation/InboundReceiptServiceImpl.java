package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.service.InboundReceiptService;
import com.cognizant.logitrack.client.NotificationClient;
import com.cognizant.logitrack.client.PurchaseOrderClient;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.InboundReceiptDTO;
import com.cognizant.logitrack.dto.NotificationDTO;
import com.cognizant.logitrack.dto.PurchaseOrderDTO;
import com.cognizant.logitrack.entity.InboundReceipt;
import com.cognizant.logitrack.entity.WarehouseInventory;
import com.cognizant.logitrack.enums.NotificationCategory;
import com.cognizant.logitrack.enums.ReceiptStatus;
import com.cognizant.logitrack.repository.InboundReceiptRepository;
import com.cognizant.logitrack.repository.WarehouseInventoryRepository;
import com.cognizant.logitrack.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InboundReceiptServiceImpl implements InboundReceiptService {
    private final InboundReceiptRepository inboundReceiptRepository;
    private final PurchaseOrderClient purchaseOrderClient;
    private final WarehouseInventoryRepository warehouseInventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final NotificationClient notificationClient;

    public InboundReceiptServiceImpl(
            InboundReceiptRepository inboundReceiptRepository,
            PurchaseOrderClient purchaseOrderClient,
            WarehouseInventoryRepository warehouseInventoryRepository,
            WarehouseRepository warehouseRepository,
            NotificationClient notificationClient) {
        this.inboundReceiptRepository = inboundReceiptRepository;
        this.purchaseOrderClient = purchaseOrderClient;
        this.warehouseInventoryRepository = warehouseInventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.notificationClient = notificationClient;
    }

    // Best-effort notification; never fails the main transaction.
    private void sendNotification(Integer userId, String message, NotificationCategory category) {
        if (userId == null) return;
        try {
            notificationClient.sendNotification(
                    NotificationDTO.builder().userId(userId).message(message).category(category).build());
        } catch (Exception e) {
            log.warn("Failed to send notification to user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public InboundReceiptDTO createReceipt(InboundReceiptDTO dto) {
        if (!warehouseRepository.existsById(dto.getWarehouseId())) {
            throw new BadRequestException("Warehouse does not exist: " + dto.getWarehouseId());
        }
        InboundReceipt receipt = InboundReceipt.builder().supplierOrderId(dto.getSupplierOrderId()).warehouseId(dto.getWarehouseId()).receivedDate(dto.getReceivedDate()).receivedBy(dto.getReceivedBy()).status(ReceiptStatus.PENDING).build();
        InboundReceipt saved = inboundReceiptRepository.save(receipt);
        return toDTO(saved);
    }

    @Override
    public List<InboundReceiptDTO> getByWarehouse(Integer warehouseId) {
        return inboundReceiptRepository.findByWarehouseId(warehouseId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<InboundReceiptDTO> getAllReceipts() {
        return inboundReceiptRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public InboundReceiptDTO updateStatus(Integer id, ReceiptStatus status) {
        InboundReceipt receipt = inboundReceiptRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inbound receipt not found with id: " + id));
        ReceiptStatus previousStatus = receipt.getStatus();
        receipt.setStatus(status);
        InboundReceipt saved = inboundReceiptRepository.save(receipt);

        if (previousStatus != status) {
            sendNotification(saved.getReceivedBy(),
                    "Inbound receipt #" + id + " is now " + status,
                    NotificationCategory.WAREHOUSE);
        }

        if (status == ReceiptStatus.RECEIVED && previousStatus != ReceiptStatus.RECEIVED) {
            try {
                PurchaseOrderDTO po = purchaseOrderClient.getPurchaseOrderById(receipt.getSupplierOrderId());
                if (po != null && po.getLineItems() != null) {
                    String[] items = po.getLineItems().split(",");
                    for (String item : items) {
                        String[] parts = item.split("[:\\-]");
                        if (parts.length == 2) {
                            String sku = parts[0].trim();
                            int qty = Integer.parseInt(parts[1].trim());

                            List<WarehouseInventory> inventories = warehouseInventoryRepository.findBySku(sku);
                            WarehouseInventory inv = inventories.stream()
                                    .filter(i -> i.getWarehouseId().equals(receipt.getWarehouseId()))
                                    .findFirst()
                                    .orElse(null);

                            if (inv != null) {
                                inv.setQuantityOnHand((inv.getQuantityOnHand() == null ? 0 : inv.getQuantityOnHand()) + qty);
                                warehouseInventoryRepository.save(inv);
                            } else {
                                WarehouseInventory newInv = WarehouseInventory.builder().sku(sku).productName("Product " + sku).warehouseId(receipt.getWarehouseId()).binLocation("RECEIVING").quantityOnHand(qty).quantityReserved(0).build();
                                warehouseInventoryRepository.save(newInv);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to auto-update inventory for receipt {}: {}", id, e.getMessage());
            }
        }
        return toDTO(saved);
    }

    private InboundReceiptDTO toDTO(InboundReceipt r) {
        return InboundReceiptDTO.builder().receiptId(r.getReceiptId()).supplierOrderId(r.getSupplierOrderId()).warehouseId(r.getWarehouseId()).receivedDate(r.getReceivedDate()).receivedBy(r.getReceivedBy()).status(r.getStatus()).build();
    }
}
