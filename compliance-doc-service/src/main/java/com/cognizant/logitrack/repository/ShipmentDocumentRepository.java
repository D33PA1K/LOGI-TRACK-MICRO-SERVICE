package com.cognizant.logitrack.repository;

import com.cognizant.logitrack.entity.ShipmentDocument;
import com.cognizant.logitrack.enums.DocumentStatus;
import com.cognizant.logitrack.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentDocumentRepository extends JpaRepository<ShipmentDocument, Integer> {
    List<ShipmentDocument> findByShipmentId(Integer shipmentId);
    List<ShipmentDocument> findByDocumentType(DocumentType documentType);
    List<ShipmentDocument> findByStatus(DocumentStatus status);
}

