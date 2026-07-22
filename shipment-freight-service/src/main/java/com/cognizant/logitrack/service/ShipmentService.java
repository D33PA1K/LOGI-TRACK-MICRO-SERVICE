package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.DeliveryEventDTO;
import com.cognizant.logitrack.dto.ShipmentDTO;
import com.cognizant.logitrack.enums.ShipmentStatus;
import java.util.List;

public interface ShipmentService {
    ShipmentDTO createShipment(ShipmentDTO dto);
    ShipmentDTO getById(Integer id);
    List<ShipmentDTO> getAllShipments();
    ShipmentDTO updateShipmentStatus(Integer id, ShipmentStatus status);
    ShipmentDTO dispatchShipment(Integer id);
    DeliveryEventDTO addDeliveryEvent(Integer shipmentId, DeliveryEventDTO dto);
    List<DeliveryEventDTO> getEventsByShipment(Integer shipmentId);
}
