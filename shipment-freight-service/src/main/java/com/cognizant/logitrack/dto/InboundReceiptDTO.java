package com.cognizant.logitrack.dto;

import com.cognizant.logitrack.enums.ReceiptStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundReceiptDTO {
    private Integer receiptId;
    @NotNull
    private Integer supplierOrderId;
    @NotNull
    private Integer warehouseId;
    private LocalDate receivedDate;
    private Integer receivedBy;
    private ReceiptStatus status;
}
