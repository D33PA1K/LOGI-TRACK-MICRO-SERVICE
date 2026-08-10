package com.cognizant.logitrack.dto;

import com.cognizant.logitrack.enums.PickListStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickListDTO {
    private Integer pickListId;
    @NotNull
    private Integer freightOrderId;
    @NotNull
    private Integer warehouseId;
    private Integer assignedTo;

    /** Optional: the SKU being picked. When set with {@link #quantity}, stock is reserved. */
    private String sku;

    /** Optional: units to pick. Must be positive when supplied. */
    @Positive
    private Integer quantity;

    private PickListStatus status;
    private LocalDate createdDate;
}
