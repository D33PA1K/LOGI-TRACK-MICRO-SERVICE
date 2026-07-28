package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.ShipmentDocumentDTO;
import com.cognizant.logitrack.enums.DocumentStatus;
import com.cognizant.logitrack.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

public interface ShipmentDocumentService {
    ShipmentDocumentDTO uploadDocument(ShipmentDocumentDTO dto);
    ShipmentDocumentDTO uploadDocument(MultipartFile file, Integer shipmentId, DocumentType documentType, LocalDate submittedDate);
    ShipmentDocumentDTO updateDocumentStatus(Integer id, DocumentStatus status);
    List<ShipmentDocumentDTO> getDocsByShipment(Integer shipmentId);
    ShipmentDocumentDTO getById(Integer id);
}
