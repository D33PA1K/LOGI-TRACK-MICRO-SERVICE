package com.cognizant.logitrack.dto;

import com.cognizant.logitrack.enums.PickListStatus;
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
public class PickListDTO {
    private Integer pickListId;
    @NotNull
    private Integer freightOrderId;
    @NotNull
    private Integer warehouseId;
    private Integer assignedTo;
    private PickListStatus status;
    private LocalDate createdDate;
}
