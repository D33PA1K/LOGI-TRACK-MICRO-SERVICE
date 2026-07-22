package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.service.ShipmentDocumentService;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.ShipmentDocumentDTO;
import com.cognizant.logitrack.entity.ShipmentDocument;
import com.cognizant.logitrack.enums.DocumentStatus;
import com.cognizant.logitrack.repository.ShipmentDocumentRepository;
import com.cognizant.logitrack.client.ShipmentClient;
import com.cognizant.logitrack.dto.ShipmentDTO;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShipmentDocumentServiceImpl implements ShipmentDocumentService {
    private final ShipmentDocumentRepository documentRepository;
    private final ShipmentClient shipmentClient;

    public ShipmentDocumentServiceImpl(ShipmentDocumentRepository documentRepository, ShipmentClient shipmentClient) {
        this.documentRepository = documentRepository;
        this.shipmentClient = shipmentClient;
    }

    @Override
    public ShipmentDocumentDTO uploadDocument(ShipmentDocumentDTO dto) {
        try {
            ShipmentDTO shipment = shipmentClient.getShipmentById(dto.getShipmentId());
            if (shipment == null) {
                throw new com.cognizant.logitrack.exception.BadRequestException("Shipment does not exist");
            }
        } catch (Exception e) {
            throw new com.cognizant.logitrack.exception.BadRequestException("Invalid or unavailable shipmentId: " + dto.getShipmentId());
        }
        
        ShipmentDocument document = ShipmentDocument.builder()
                .shipmentId(dto.getShipmentId())
                .documentType(dto.getDocumentType())
                .filePath(dto.getFilePath())
                .submittedDate(dto.getSubmittedDate())
                .status(DocumentStatus.PENDING)
                .build();
        ShipmentDocument saved = documentRepository.save(document);
        log.info("Document uploaded: id={}, shipmentId={}", saved.getDocumentId(), saved.getShipmentId());
        return toDTO(saved);
    }

    @Override
    public ShipmentDocumentDTO updateDocumentStatus(Integer id, DocumentStatus status) {
        ShipmentDocument document = findEntity(id);
        document.setStatus(status);
        return toDTO(documentRepository.save(document));
    }

    @Override
    public List<ShipmentDocumentDTO> getDocsByShipment(Integer shipmentId) {
        return documentRepository.findByShipmentId(shipmentId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ShipmentDocumentDTO getById(Integer id) {
        return toDTO(findEntity(id));
    }

    private ShipmentDocument findEntity(Integer id) {
        return documentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Shipment document not found with id: " + id));
    }

    private ShipmentDocumentDTO toDTO(ShipmentDocument d) {
        return ShipmentDocumentDTO.builder().documentId(d.getDocumentId()).shipmentId(d.getShipmentId()).documentType(d.getDocumentType()).filePath(d.getFilePath()).submittedDate(d.getSubmittedDate()).status(d.getStatus()).build();
    }
}
