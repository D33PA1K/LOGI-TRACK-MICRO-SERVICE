package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.client.RouteClient;
import com.cognizant.logitrack.dto.RouteDTO;
import com.cognizant.logitrack.service.FreightOrderService;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.FreightOrderDTO;
import com.cognizant.logitrack.entity.FreightOrder;
import com.cognizant.logitrack.enums.FreightOrderStatus;
import com.cognizant.logitrack.repository.FreightOrderRepository;
import com.cognizant.logitrack.client.IdentityClient;
import com.cognizant.logitrack.client.PurchaseOrderClient;
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
public class FreightOrderServiceImpl implements FreightOrderService {

    private final FreightOrderRepository freightOrderRepository;
    private final RouteClient routeClient;
    private final IdentityClient identityClient;
    private final PurchaseOrderClient purchaseOrderClient;

    public FreightOrderServiceImpl(
            FreightOrderRepository freightOrderRepository,
            RouteClient routeClient, IdentityClient identityClient, PurchaseOrderClient purchaseOrderClient
    ) {
        this.freightOrderRepository = freightOrderRepository;
        this.routeClient = routeClient;
        this.identityClient = identityClient;
        this.purchaseOrderClient = purchaseOrderClient;
    }

    @Override
    public FreightOrderDTO createFreightOrder(FreightOrderDTO dto) {

        if (dto.getOriginLocationId().equals(dto.getDestinationLocationId())) {
            throw new BadRequestException("Origin location and destination location cannot be same");
        }

        if (dto.getWeight() == null || dto.getWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Weight must be greater than zero");
        }

        if (dto.getVolume() == null || dto.getVolume().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Volume must be greater than zero");
        }

        if (dto.getRequiredDeliveryDate() == null || dto.getRequiredDeliveryDate().isBefore(LocalDate.now())|| dto.getRequiredDeliveryDate().isEqual(LocalDate.now()) ) {
            throw new BadRequestException("Required delivery date cannot be in the past or current date");
        }

                try {
            Object shipper = identityClient.getUserById(dto.getShipperId());
            if (shipper == null) throw new BadRequestException("Shipper does not exist");
        } catch (Exception e) {
            throw new BadRequestException("Invalid or unavailable shipperId: " + dto.getShipperId());
        }

        if (dto.getPoId() != null) {
            try {
                Object po = purchaseOrderClient.getPurchaseOrderById(dto.getPoId());
                if (po == null) throw new BadRequestException("Purchase Order does not exist");
            } catch (Exception e) {
                throw new BadRequestException("Invalid or unavailable poId: " + dto.getPoId());
            }
        }
        
        RouteDTO route = null;
        try {
            route = routeClient.searchRoute(
                    dto.getOriginLocationId(),
                    dto.getDestinationLocationId(),
                    "ACTIVE"
            );
        } catch (Exception e) {
            log.warn("Failed to find active route: {}", e.getMessage());
        }

        if (route == null) {
            throw new BadRequestException(
                    "No active route found for originLocationId: "
                            + dto.getOriginLocationId()
                            + " and destinationLocationId: "
                            + dto.getDestinationLocationId()
            );
        }

        FreightOrder order = FreightOrder.builder()
                .shipperId(dto.getShipperId())
                .poId(dto.getPoId())
                .originLocationId(dto.getOriginLocationId())
                .destinationLocationId(dto.getDestinationLocationId())
                .routeId(route.getRouteId())
                .cargoDescription(dto.getCargoDescription())
                .weight(dto.getWeight())
                .volume(dto.getVolume())
                .requiredDeliveryDate(dto.getRequiredDeliveryDate())
                .status(FreightOrderStatus.BOOKED)
                .build();

        FreightOrder saved = freightOrderRepository.save(order);

        log.info("Freight order created: id={}, routeId={}",
                saved.getFreightOrderId(),
                route.getRouteId());

        return toDTO(saved);
    }

    @Override
    public FreightOrderDTO getById(Integer id) {
        return toDTO(findEntity(id));
    }

    @Override
    public List<FreightOrderDTO> getAllOrders() {
        return freightOrderRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FreightOrderDTO> getByShipper(Integer shipperId) {
        return freightOrderRepository.findByShipperId(shipperId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FreightOrderDTO updateStatus(Integer id, FreightOrderStatus status) {
        FreightOrder order = findEntity(id);
        order.setStatus(status);
        FreightOrder saved = freightOrderRepository.save(order);
        log.info("Freight order {} status updated to {}", id, status);
        return toDTO(saved);
    }

    @Override
    public FreightOrderDTO cancelOrder(Integer id) {
        FreightOrder order = findEntity(id);

        if (order.getStatus() == FreightOrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel a delivered order");
        }

        order.setStatus(FreightOrderStatus.CANCELLED);
        FreightOrder saved = freightOrderRepository.save(order);

        log.info("Freight order {} cancelled", id);

        return toDTO(saved);
    }

    private FreightOrder findEntity(Integer id) {
        return freightOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Freight order not found with id: " + id));
    }

    private FreightOrderDTO toDTO(FreightOrder order) {
        return FreightOrderDTO.builder()
                .freightOrderId(order.getFreightOrderId())
                .shipperId(order.getShipperId())
                .poId(order.getPoId())
                .originLocationId(order.getOriginLocationId())
                .destinationLocationId(order.getDestinationLocationId())
                .routeId(order.getRouteId())
                .cargoDescription(order.getCargoDescription())
                .weight(order.getWeight())
                .volume(order.getVolume())
                .requiredDeliveryDate(order.getRequiredDeliveryDate())
                .status(order.getStatus())
                .build();
    }
}
