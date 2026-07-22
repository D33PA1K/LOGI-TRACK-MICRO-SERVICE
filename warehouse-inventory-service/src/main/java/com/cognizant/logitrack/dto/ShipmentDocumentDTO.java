package com.cognizant.logitrack.dto;

import com.cognizant.logitrack.enums.DocumentStatus;
import com.cognizant.logitrack.enums.DocumentType;
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
public class ShipmentDocumentDTO {
    private Integer documentId;
    @NotNull
    private Integer shipmentId;
    @NotNull
    private DocumentType documentType;
    private String filePath;
    private LocalDate submittedDate;
    private DocumentStatus status;
}
