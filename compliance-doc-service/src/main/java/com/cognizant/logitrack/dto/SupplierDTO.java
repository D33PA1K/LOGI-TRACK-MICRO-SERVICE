package com.cognizant.logitrack.dto;

import com.cognizant.logitrack.enums.SupplierStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDTO {
    private Integer supplierId;
    @NotBlank
    private String name;
    private String category;
    private String contactDetails;
    private Integer leadTimeDays;
    private SupplierStatus status;
}
