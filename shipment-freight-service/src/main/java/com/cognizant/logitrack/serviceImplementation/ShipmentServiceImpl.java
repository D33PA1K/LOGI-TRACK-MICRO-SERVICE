package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.service.ShipmentService;
import com.cognizant.logitrack.service.ShipmentStatusTransitions;
import com.cognizant.logitrack.client.*;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.exception.ServiceUnavailableException;
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
    private final IdentityClient identityClient;

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
            RouteClient routeClient,
            IdentityClient identityClient
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
        this.identityClient = identityClient;
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

        // A downstream failure surfaces its own message via the Feign fallback
        // (503 "Carrier service unavailable" vs 400 "Carrier #x not found").
        CarrierDTO carrier = carrierClient.getCarrierById(dto.getCarrierId());
        if (carrier == null) {
            throw new BadRequestException("Carrier not found: " + dto.getCarrierId());
        }
        if (carrier.getStatus() != CarrierStatus.ACTIVE) {
            throw new BadRequestException("Carrier is not active: " + dto.getCarrierId());
        }

        LocalDate planDate = dto.getDispatchDate() != null ? dto.getDispatchDate() : LocalDate.now();

        // In a real microservice, we would pass query params to rateCardClient, but for now we assume 
        // rateCardId is either passed in DTO or we fetch it. We'll simplify to fetch by ID if passed, or just mock.
        // Assuming dto has rateCardId
        RateCardDTO rateCard = rateCardClient.getRateCardById(dto.getRateCardId());
        if (rateCard == null) {
            throw new BadRequestException("Rate card not found for ID: " + dto.getRateCardId());
        }

        // If a driver is assigned, they must exist in the users table AND have
        // the DRIVER role (mirrors the shipper validation on freight orders).
        if (dto.getDriverId() != null) {
            UserDTO driver = identityClient.getUserById(dto.getDriverId());
            if (driver == null) {
                throw new BadRequestException(
                        "No user found with id " + dto.getDriverId() + ". Give a valid driver ID.");
            }
            if (driver.getRole() != Role.DRIVER) {
                throw new BadRequestException(
                        "User " + dto.getDriverId() + " has the role " + driver.getRole()
                                + ", not DRIVER. Give a valid driver ID.");
            }
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
        } catch (ServiceUnavailableException e) {
            throw e;
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
        } catch (ServiceUnavailableException e) {
            throw e;
        } catch(Exception e) {
            log.warn("Could not reach documents service during dispatch", e);
        }

        if (hasBlockingDocs) {
            throw new BadRequestException("Cannot dispatch: PENDING/SUBMITTED docs exist");
        }

        // Gate 3: Carrier Active
        if (shipment.getCarrierId() != null) {
            try {
                CarrierDTO carrier = carrierClient.getCarrierById(shipment.getCarrierId());
                if (carrier.getStatus() != CarrierStatus.ACTIVE) {
                    throw new BadRequestException("Cannot dispatch: carrier is not active");
                }
            } catch (BadRequestException | ServiceUnavailableException e) {
                throw e;
            } catch(Exception e) {
                log.warn("Could not verify carrier during dispatch", e);
            }
        }

        // Gate 4: Compliance Flags
        boolean hasOpenFlags = false;
        try {
            List<ComplianceFlagDTO> flags = complianceFlagClient.getByShipment(shipment.getShipmentId());
            hasOpenFlags = flags.stream().anyMatch(f -> f.getStatus() == FlagStatus.OPEN);
        } catch (ServiceUnavailableException e) {
            throw e;
        } catch(Exception e) {
            log.warn("Could not reach compliance service during dispatch", e);
        }

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

        // Guard the lifecycle before touching anything: an illegal transition
        // would otherwise corrupt the delivery history the analytics module reads.
        if (!ShipmentStatusTransitions.isAllowed(previous, status)) {
            throw new BadRequestException(
                    ShipmentStatusTransitions.describeRejection(previous, status));
        }

        shipment.setStatus(status);

        if (status == ShipmentStatus.DELIVERED) {
            shipment.setActualArrival(LocalDate.now());
            if (shipment.getFreightOrder() != null) {
                shipment.getFreightOrder().setStatus(FreightOrderStatus.DELIVERED);
                freightOrderRepository.save(shipment.getFreightOrder());
                sendNotification(shipment.getFreightOrder().getShipperId(),
                        "Shipment #" + id + " was delivered", NotificationCategory.SHIPMENT);
            }
            sendNotification(shipment.getDriverId(),
                    "Shipment #" + id + " marked delivered", NotificationCategory.SHIPMENT);
        }

        if ((status == ShipmentStatus.DELAYED || status == ShipmentStatus.EXCEPTION)
                && previous != status) {
            String message = "Shipment #" + id + " is now " + status;
            if (shipment.getFreightOrder() != null) {
                sendNotification(shipment.getFreightOrder().getShipperId(), message, NotificationCategory.SHIPMENT);
            }
            sendNotification(shipment.getDriverId(), message, NotificationCategory.SHIPMENT);
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
        DeliveryEvent savedEvent = deliveryEventRepository.save(event);

        // Notify the shipper and the assigned driver about the delivery update.
        String message = "Delivery update: " + dto.getEventType()
                + " recorded for shipment #" + shipmentId;
        if (shipment.getFreightOrder() != null) {
            sendNotification(shipment.getFreightOrder().getShipperId(), message, NotificationCategory.SHIPMENT);
        }
        sendNotification(shipment.getDriverId(), message, NotificationCategory.SHIPMENT);

        return toEventDTO(savedEvent);
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

    /**
     * A rate card is only priced for a weight band, so applying one outside its
     * band would quote a cost the carrier never agreed to. Previously an empty
     * stub, which meant weightSlab was decorative.
     *
     * Accepted formats (units are the same as the freight order's weight):
     *   "0-1000"   inclusive range
     *   "1000+"    open-ended minimum
     *   "&lt;=500"     maximum only
     * An unrecognised or blank slab is treated as "no restriction" rather than
     * rejecting the shipment, so existing rate cards keep working.
     */
    private void validateWeightSlab(BigDecimal weight, String weightSlab) {
        if (weight == null || weightSlab == null || weightSlab.isBlank()) {
            return;
        }

        String slab = weightSlab.trim().replace(" ", "");

        try {
            if (slab.endsWith("+")) {
                BigDecimal min = new BigDecimal(slab.substring(0, slab.length() - 1));
                requireAtLeast(weight, min, weightSlab);
                return;
            }

            if (slab.startsWith("<=")) {
                BigDecimal max = new BigDecimal(slab.substring(2));
                requireAtMost(weight, max, weightSlab);
                return;
            }

            if (slab.startsWith(">=")) {
                BigDecimal min = new BigDecimal(slab.substring(2));
                requireAtLeast(weight, min, weightSlab);
                return;
            }

            int dash = slab.indexOf('-', 1);

            if (dash > 0) {
                BigDecimal min = new BigDecimal(slab.substring(0, dash));
                BigDecimal max = new BigDecimal(slab.substring(dash + 1));
                requireAtLeast(weight, min, weightSlab);
                requireAtMost(weight, max, weightSlab);
            }
        } catch (NumberFormatException e) {
            // Not a slab format we understand — do not block the shipment on it.
            log.warn("Unrecognised rate card weightSlab '{}'; skipping weight validation", weightSlab);
        }
    }

    private void requireAtLeast(BigDecimal weight, BigDecimal min, String slab) {
        if (weight.compareTo(min) < 0) {
            throw new BadRequestException("Freight weight " + weight.stripTrailingZeros().toPlainString()
                    + " is below the rate card's weight slab (" + slab + "). Choose a rate card for this weight.");
        }
    }

    private void requireAtMost(BigDecimal weight, BigDecimal max, String slab) {
        if (weight.compareTo(max) > 0) {
            throw new BadRequestException("Freight weight " + weight.stripTrailingZeros().toPlainString()
                    + " exceeds the rate card's weight slab (" + slab + "). Choose a rate card for this weight.");
        }
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
