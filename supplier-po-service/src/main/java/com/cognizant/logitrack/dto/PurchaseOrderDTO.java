package com.cognizant.logitrack.dto;

import com.cognizant.logitrack.enums.POStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDTO {
    private Integer poId;
    @NotNull
    private Integer supplierId;
    private Integer warehouseId;
    private String lineItems;
    private BigDecimal totalValue;
    private LocalDate orderDate;
    private LocalDate expectedDelivery;
    private POStatus status;
}
