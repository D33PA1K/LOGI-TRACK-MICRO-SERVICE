package com.cognizant.logitrack.entity;

import com.cognizant.logitrack.enums.ReceiptStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inbound_receipts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer receiptId;
    private Integer supplierOrderId;
    private Integer warehouseId;
    private LocalDate receivedDate;
    private Integer receivedBy;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReceiptStatus status = ReceiptStatus.PENDING;
}
