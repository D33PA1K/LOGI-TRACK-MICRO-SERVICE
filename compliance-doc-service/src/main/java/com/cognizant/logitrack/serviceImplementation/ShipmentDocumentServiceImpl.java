package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.service.ShipmentDocumentService;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.exception.ServiceUnavailableException;
import com.cognizant.logitrack.dto.ShipmentDocumentDTO;
import com.cognizant.logitrack.entity.ShipmentDocument;
import com.cognizant.logitrack.enums.DocumentStatus;
import com.cognizant.logitrack.enums.DocumentType;
import com.cognizant.logitrack.repository.ShipmentDocumentRepository;
import com.cognizant.logitrack.client.ShipmentClient;
import com.cognizant.logitrack.dto.ShipmentDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShipmentDocumentServiceImpl implements ShipmentDocumentService {
    private final ShipmentDocumentRepository documentRepository;
    private final ShipmentClient shipmentClient;

    // Base directory under which uploaded files are stored. A relative value keeps
    // the persisted filePath portable across environments.
    @Value("${document.storage.location:uploads/shipment-documents}")
    private String storageLocation;

    public ShipmentDocumentServiceImpl(ShipmentDocumentRepository documentRepository, ShipmentClient shipmentClient) {
        this.documentRepository = documentRepository;
        this.shipmentClient = shipmentClient;
    }

    @Override
    public ShipmentDocumentDTO uploadDocument(ShipmentDocumentDTO dto) {
        validateShipmentExists(dto.getShipmentId());

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
    public ShipmentDocumentDTO uploadDocument(MultipartFile file, Integer shipmentId,
                                              DocumentType documentType, LocalDate submittedDate) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("A file must be selected for upload");
        }
        validateShipmentExists(shipmentId);

        String relativePath = storeFile(file);

        ShipmentDocument document = ShipmentDocument.builder()
                .shipmentId(shipmentId)
                .documentType(documentType)
                .filePath(relativePath)
                .submittedDate(submittedDate)
                .status(DocumentStatus.PENDING)
                .build();
        ShipmentDocument saved = documentRepository.save(document);
        log.info("Document uploaded (file): id={}, shipmentId={}, path={}",
                saved.getDocumentId(), saved.getShipmentId(), relativePath);
        return toDTO(saved);
    }

    // Validates the referenced shipment exists via the shipment-freight-service.
    private void validateShipmentExists(Integer shipmentId) {
        try {
            ShipmentDTO shipment = shipmentClient.getShipmentById(shipmentId);
            if (shipment == null) {
                throw new BadRequestException("Shipment does not exist");
            }
        } catch (BadRequestException | ServiceUnavailableException e) {
            // Preserve the fallback's specific meaning: 400 "not found" or 503 "unavailable".
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Invalid or unavailable shipmentId: " + shipmentId);
        }
    }

    // Persists the uploaded file under the configured storage directory and
    // returns a relative path (e.g. "uploads/shipment-documents/<uuid>_name.pdf").
    private String storeFile(MultipartFile file) {
        try {
            Path baseDir = Paths.get(storageLocation);
            Files.createDirectories(baseDir);

            String originalName = StringUtils.cleanPath(
                    file.getOriginalFilename() == null ? "document" : file.getOriginalFilename());
            String storedName = UUID.randomUUID() + "_" + originalName;

            Path target = baseDir.resolve(storedName).toAbsolutePath();
            file.transferTo(target);

            // Store with forward slashes so the path is consistent across OSes.
            return Paths.get(storageLocation, storedName).toString().replace('\\', '/');
        } catch (IOException e) {
            throw new BadRequestException("Failed to store uploaded file: " + e.getMessage());
        }
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
