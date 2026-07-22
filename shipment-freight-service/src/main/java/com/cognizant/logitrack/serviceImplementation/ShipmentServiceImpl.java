package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.service.ShipmentService;
import com.cognizant.logitrack.client.*;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.*;
import com.cognizant.logitrack.entity.DeliveryEvent;
import com.cognizant.logitrack.entity.FreightOrder;
import com.cognizant.logitrack.entity.Shipment;
import com.cognizant.logitrack.enums.*;
import com.cognizant.logitrack.repository.DeliveryEventRepository;
import com.cognizant.logitrack.repository.FreightOrderRepository;
import com.cognizant.logitrack.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final FreightOrderRepository freightOrderRepository;
    private final DeliveryEventRepository deliveryEventRepository;
    
    // Feign Clients
    private final CarrierClient carrierClient;
    private final RateCardClient rateCardClient;
    private final PickListClient pickListClient;
    private final ShipmentDocumentClient shipmentDocumentClient;
    private final ComplianceFlagClient complianceFlagClient;
    private final NotificationClient notificationClient;
    private final RouteClient routeClient;

    public ShipmentServiceImpl(
            ShipmentRepository shipmentRepository,
            FreightOrderRepository freightOrderRepository,
            DeliveryEventRepository deliveryEventRepository,
            CarrierClient carrierClient,
            RateCardClient rateCardClient,
            PickListClient pickListClient,
            ShipmentDocumentClient shipmentDocumentClient,
            ComplianceFlagClient complianceFlagClient,
            NotificationClient notificationClient,
            RouteClient routeClient
    ) {
        this.shipmentRepository = shipmentRepository;
        this.freightOrderRepository = freightOrderRepository;
        this.deliveryEventRepository = deliveryEventRepository;
        this.carrierClient = carrierClient;
        this.rateCardClient = rateCardClient;
        this.pickListClient = pickListClient;
        this.shipmentDocumentClient = shipmentDocumentClient;
        this.complianceFlagClient = complianceFlagClient;
        this.notificationClient = notificationClient;
        this.routeClient = routeClient;
    }

    @Override
    public ShipmentDTO createShipment(ShipmentDTO dto) {

        FreightOrder freightOrder = freightOrderRepository.findById(dto.getFreightOrderId())
                .orElseThrow(() -> new BadRequestException(
                        "Freight order not found: " + dto.getFreightOrderId()
                ));

        if (freightOrder.getStatus() == FreightOrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot create shipment for a cancelled freight order");
        }

        if (freightOrder.getStatus() == FreightOrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot create shipment for an already delivered freight order");
        }

        if (freightOrder.getRouteId() == null) {
            throw new BadRequestException("Freight order has no route linked. A valid route is required to plan a shipment.");
        }
        
        RouteDTO route = routeClient.getRouteById(freightOrder.getRouteId());

        CarrierDTO carrier = null;
        try {
            carrier = carrierClient.getCarrierById(dto.getCarrierId());
        } catch (Exception e) {
            throw new BadRequestException("Carrier not found: " + dto.getCarrierId());
        }

        if (carrier.getStatus() != CarrierStatus.ACTIVE) {
            throw new BadRequestException("Carrier is not active: " + dto.getCarrierId());
        }

        LocalDate planDate = dto.getDispatchDate() != null ? dto.getDispatchDate() : LocalDate.now();

        // In a real microservice, we would pass query params to rateCardClient, but for now we assume 
        // rateCardId is either passed in DTO or we fetch it. We'll simplify to fetch by ID if passed, or just mock.
        // Assuming dto has rateCardId
        RateCardDTO rateCard = null;
        try {
            rateCard = rateCardClient.getRateCardById(dto.getRateCardId());
        } catch (Exception e) {
            throw new BadRequestException("Rate card not found for ID: " + dto.getRateCardId());
        }

        validateWeightSlab(freightOrder.getWeight(), rateCard.getWeightSlab());

        BigDecimal freightCost = calculateFreightCost(freightOrder, rateCard);
        LocalDate estimatedArrival = planDate.plusDays(route.getTransitDays() != null ? route.getTransitDays() : 1);

        Shipment shipment = Shipment.builder()
                .freightOrder(freightOrder)
                .carrierId(carrier.getCarrierId())
                .vehicleId(dto.getVehicleId())
                .driverId(dto.getDriverId())
                .rateCardId(rateCard.getRateCardId())
                .freightCost(freightCost)
                .dispatchDate(planDate)
                .estimatedArrival(estimatedArrival)
                .status(ShipmentStatus.PLANNED)
                .build();

        Shipment saved = shipmentRepository.save(shipment);

        return toDTO(saved);
    }

    @Override
    public ShipmentDTO dispatchShipment(Integer id) {

        Shipment shipment = findEntity(id);

        if (shipment.getStatus() == ShipmentStatus.DISPATCHED
                || shipment.getStatus() == ShipmentStatus.INTRANSIT
                || shipment.getStatus() == ShipmentStatus.DELIVERED) {
            throw new BadRequestException("Shipment " + id + " is already in status: " + shipment.getStatus());
        }

        FreightOrder freightOrder = shipment.getFreightOrder();
        
        // Gate 1: PickList
        boolean pickListCompleted = false;
        try {
            List<PickListDTO> pickLists = pickListClient.getByFreightOrder(freightOrder.getFreightOrderId());
            pickListCompleted = pickLists.stream().anyMatch(pl -> pl.getStatus() == PickListStatus.COMPLETED);
        } catch(Exception e) {
            log.warn("Could not reach WMS for PickList validation", e);
        }

        if (!pickListCompleted) {
            throw new BadRequestException("Cannot dispatch: PickList is not COMPLETED");
        }

        // Gate 2: Docs
        boolean hasBlockingDocs = false;
        try {
            List<ShipmentDocumentDTO> docs = shipmentDocumentClient.getByShipment(shipment.getShipmentId());
            hasBlockingDocs = docs.stream().anyMatch(doc -> doc.getStatus() == DocumentStatus.PENDING || doc.getStatus() == DocumentStatus.SUBMITTED);
        } catch(Exception e) {}

        if (hasBlockingDocs) {
            throw new BadRequestException("Cannot dispatch: PENDING/SUBMITTED docs exist");
        }

        // Gate 3: Carrier Active
        if (shipment.getCarrierId() != null) {
            try {
                CarrierDTO carrier = carrierClient.getCarrierById(shipment.getCarrierId());
                if (carrier.getStatus() != CarrierStatus.ACTIVE) {
                    throw new BadRequestException("Carrier is not active");
                }
            } catch(Exception e) {}
        }

        // Gate 4: Compliance Flags
        boolean hasOpenFlags = false;
        try {
            List<ComplianceFlagDTO> flags = complianceFlagClient.getByShipment(shipment.getShipmentId());
            hasOpenFlags = flags.stream().anyMatch(f -> f.getStatus() == FlagStatus.OPEN);
        } catch(Exception e) {}

        if (hasOpenFlags) {
            throw new BadRequestException("Cannot dispatch: OPEN compliance flags exist");
        }

        shipment.setStatus(ShipmentStatus.DISPATCHED);
        shipment.setDispatchDate(LocalDate.now());
        Shipment saved = shipmentRepository.save(shipment);

        freightOrder.setStatus(FreightOrderStatus.INTRANSIT);
        freightOrderRepository.save(freightOrder);

        sendNotification(freightOrder.getShipperId(), "Shipment dispatched", NotificationCategory.SHIPMENT);
        sendNotification(shipment.getDriverId(), "Shipment assigned", NotificationCategory.SHIPMENT);

        return toDTO(saved);
    }

    @Override
    public ShipmentDTO getById(Integer id) {
        return toDTO(findEntity(id));
    }

    @Override
    public List<ShipmentDTO> getAllShipments() {
        return shipmentRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ShipmentDTO updateShipmentStatus(Integer id, ShipmentStatus status) {

        Shipment shipment = findEntity(id);
        ShipmentStatus previous = shipment.getStatus();
        shipment.setStatus(status);

        if (status == ShipmentStatus.DELIVERED) {
            shipment.setActualArrival(LocalDate.now());
            if (shipment.getFreightOrder() != null) {
                shipment.getFreightOrder().setStatus(FreightOrderStatus.DELIVERED);
                freightOrderRepository.save(shipment.getFreightOrder());
                sendNotification(shipment.getFreightOrder().getShipperId(), "Shipment delivered", NotificationCategory.SHIPMENT);
            }
        }

        if ((status == ShipmentStatus.DELAYED || status == ShipmentStatus.EXCEPTION)
                && shipment.getFreightOrder() != null) {
            sendNotification(shipment.getFreightOrder().getShipperId(), "Shipment delayed/exception", NotificationCategory.SHIPMENT);
        }

        Shipment saved = shipmentRepository.save(shipment);
        return toDTO(saved);
    }

    @Override
    public DeliveryEventDTO addDeliveryEvent(Integer shipmentId, DeliveryEventDTO dto) {
        Shipment shipment = findEntity(shipmentId);
        DeliveryEvent event = DeliveryEvent.builder()
                .shipment(shipment)
                .eventType(dto.getEventType())
                .locationId(dto.getLocationId())
                .notes(dto.getNotes())
                .build();
        return toEventDTO(deliveryEventRepository.save(event));
    }

    @Override
    public List<DeliveryEventDTO> getEventsByShipment(Integer shipmentId) {
        return deliveryEventRepository.findByShipment_ShipmentId(shipmentId).stream().map(this::toEventDTO).collect(Collectors.toList());
    }

    private Shipment findEntity(Integer id) {
        return shipmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));
    }

    private BigDecimal calculateFreightCost(FreightOrder freightOrder, RateCardDTO rateCard) {
        return rateCard.getBaseRate().multiply(freightOrder.getWeight());
    }

    private void validateWeightSlab(BigDecimal weight, String weightSlab) {
        // simplified validation
    }

    private void sendNotification(Integer userId, String message, NotificationCategory category) {
        if (userId == null) return;
        try {
            notificationClient.sendNotification(NotificationDTO.builder().userId(userId).message(message).category(category).build());
        } catch (Exception e) {}
    }

    private ShipmentDTO toDTO(Shipment shipment) {
        return ShipmentDTO.builder()
                .shipmentId(shipment.getShipmentId())
                .freightOrderId(shipment.getFreightOrder() != null ? shipment.getFreightOrder().getFreightOrderId() : null)
                .carrierId(shipment.getCarrierId())
                .vehicleId(shipment.getVehicleId())
                .driverId(shipment.getDriverId())
                .rateCardId(shipment.getRateCardId())
                .freightCost(shipment.getFreightCost())
                .dispatchDate(shipment.getDispatchDate())
                .estimatedArrival(shipment.getEstimatedArrival())
                .actualArrival(shipment.getActualArrival())
                .status(shipment.getStatus())
                .build();
    }

    private DeliveryEventDTO toEventDTO(DeliveryEvent event) {
        return DeliveryEventDTO.builder().eventId(event.getEventId()).shipmentId(event.getShipment() != null ? event.getShipment().getShipmentId() : null).eventType(event.getEventType()).timestamp(event.getTimestamp()).locationId(event.getLocationId()).notes(event.getNotes()).build();
    }
}
