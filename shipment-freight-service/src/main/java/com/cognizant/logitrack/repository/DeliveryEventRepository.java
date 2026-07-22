package com.cognizant.logitrack.repository;

import com.cognizant.logitrack.entity.DeliveryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryEventRepository extends JpaRepository<DeliveryEvent, Integer> {
    List<DeliveryEvent> findByShipment_ShipmentId(Integer shipmentId);
}
