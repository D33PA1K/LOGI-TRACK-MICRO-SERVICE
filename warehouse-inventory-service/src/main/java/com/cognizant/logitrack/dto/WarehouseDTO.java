package com.cognizant.logitrack.dto;

import com.cognizant.logitrack.enums.WarehouseStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseDTO {
    private Integer warehouseId;
    @NotBlank
    private String warehouseName;
    @NotBlank
    private String addressLine;
    @NotBlank
    private String city;
    @NotBlank
    private String state;
    @NotBlank
    private String country;
    private String postalCode;
    private String contactNumber;
    private WarehouseStatus status;
}
