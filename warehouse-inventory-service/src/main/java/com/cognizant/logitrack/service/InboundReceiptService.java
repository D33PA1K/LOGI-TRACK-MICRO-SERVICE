package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.InboundReceiptDTO;
import com.cognizant.logitrack.enums.ReceiptStatus;
import java.util.List;

public interface InboundReceiptService {
    InboundReceiptDTO createReceipt(InboundReceiptDTO dto);
    List<InboundReceiptDTO> getByWarehouse(Integer warehouseId);
    InboundReceiptDTO updateStatus(Integer id, ReceiptStatus status);
}
