package com.cognizant.logitrack.entity;

import com.cognizant.logitrack.enums.DocumentStatus;
import com.cognizant.logitrack.enums.DocumentType;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shipment_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer documentId;
    private Integer shipmentId;
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;
    @Column(length = 500)
    private String filePath;
    private LocalDate submittedDate;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PENDING;
}

