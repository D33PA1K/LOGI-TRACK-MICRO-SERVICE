package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.service.PurchaseOrderService;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.PurchaseOrderDTO;
import com.cognizant.logitrack.entity.PurchaseOrder;
import com.cognizant.logitrack.entity.Supplier;
import com.cognizant.logitrack.enums.POStatus;
import com.cognizant.logitrack.repository.PurchaseOrderRepository;
import com.cognizant.logitrack.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;

    public PurchaseOrderServiceImpl(PurchaseOrderRepository purchaseOrderRepository, SupplierRepository supplierRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
    }

    @Override
    public PurchaseOrderDTO createPO(PurchaseOrderDTO dto) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId()).orElseThrow(() -> new BadRequestException("Supplier not found: " + dto.getSupplierId()));
        if (supplier.getStatus() != com.cognizant.logitrack.enums.SupplierStatus.ACTIVE) {
            throw new BadRequestException("Cannot create purchase order for an inactive supplier: " + supplier.getName());
        }
        // The order date is always the current date; any client-supplied value
        // is ignored so the PO cannot be back- or future-dated.
        PurchaseOrder po = PurchaseOrder.builder().supplier(supplier).warehouseId(dto.getWarehouseId()).lineItems(dto.getLineItems()).totalValue(dto.getTotalValue()).orderDate(LocalDate.now()).expectedDelivery(dto.getExpectedDelivery()).status(POStatus.DRAFT).build();
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        log.info("Purchase order created: id={}", saved.getPoId());
        return toDTO(saved);
    }

    @Override
    public List<PurchaseOrderDTO> getPOsBySupplier(Integer supplierId) {
        return purchaseOrderRepository.findBySupplier_SupplierId(supplierId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderDTO> getPOsByWarehouse(Integer warehouseId) {
        return purchaseOrderRepository.findByWarehouseId(warehouseId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderDTO> getPOsBySupplierAndWarehouse(Integer supplierId, Integer warehouseId) {
        return purchaseOrderRepository.findBySupplier_SupplierIdAndWarehouseId(supplierId, warehouseId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public PurchaseOrderDTO updatePOStatus(Integer id, POStatus status) {
        PurchaseOrder po = findEntity(id);
        po.setStatus(status);
        return toDTO(purchaseOrderRepository.save(po));
    }

    @Override
    public PurchaseOrderDTO getById(Integer id) {
        return toDTO(findEntity(id));
    }

    private PurchaseOrder findEntity(Integer id) {
        return purchaseOrderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
    }

    private PurchaseOrderDTO toDTO(PurchaseOrder po) {
        return PurchaseOrderDTO.builder().poId(po.getPoId()).supplierId(po.getSupplier() != null ? po.getSupplier().getSupplierId() : null).warehouseId(po.getWarehouseId()).lineItems(po.getLineItems()).totalValue(po.getTotalValue()).orderDate(po.getOrderDate()).expectedDelivery(po.getExpectedDelivery()).status(po.getStatus()).build();
    }
}
