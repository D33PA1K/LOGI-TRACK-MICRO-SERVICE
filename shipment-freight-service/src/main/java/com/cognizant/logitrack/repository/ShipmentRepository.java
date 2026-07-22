package com.cognizant.logitrack.repository;

import com.cognizant.logitrack.entity.Shipment;
import com.cognizant.logitrack.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Integer> {
    List<Shipment> findByFreightOrder_FreightOrderId(Integer freightOrderId);
    List<Shipment> findByStatus(ShipmentStatus status);
}
