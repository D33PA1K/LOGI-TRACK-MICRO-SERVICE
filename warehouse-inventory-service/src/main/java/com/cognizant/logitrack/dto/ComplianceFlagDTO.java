package com.cognizant.logitrack.dto;

import com.cognizant.logitrack.enums.FlagSeverity;
import com.cognizant.logitrack.enums.FlagStatus;
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
public class ComplianceFlagDTO {
    private Integer flagId;
    @NotNull
    private Integer shipmentId;
    private String flagType;
    @NotNull
    private FlagSeverity severity;
    private LocalDate raisedDate;
    private FlagStatus status;
}
