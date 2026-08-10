package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.client.CarrierClient;
import com.cognizant.logitrack.client.FreightOrderClient;
import com.cognizant.logitrack.client.ShipmentClient;
import com.cognizant.logitrack.dto.CarrierDTO;
import com.cognizant.logitrack.dto.CarrierScorecardDTO;
import com.cognizant.logitrack.dto.FreightOrderDTO;
import com.cognizant.logitrack.dto.LogisticsReportDTO;
import com.cognizant.logitrack.dto.ReportRequestDTO;
import com.cognizant.logitrack.dto.ScopeBreakdownDTO;
import com.cognizant.logitrack.dto.ShipmentDTO;
import com.cognizant.logitrack.dto.ShipmentMetricsDTO;
import com.cognizant.logitrack.entity.LogisticsReport;
import com.cognizant.logitrack.enums.ReportScope;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.repository.LogisticsReportRepository;
import com.cognizant.logitrack.service.LogisticsReportService;
import com.cognizant.logitrack.service.ShipmentMetricsCalculator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Computes logistics KPIs from live shipment data held by another service.
 *
 * Design notes worth knowing:
 *  - Analytics owns almost no data. It reads across the service boundary through
 *    Feign (propagating the caller's own JWT) rather than sharing a database, so
 *    authorization stays enforced by the service that owns the data.
 *  - A generated report is an immutable point-in-time snapshot: the metrics JSON
 *    and the period are persisted, so re-reading an old report cannot silently
 *    change as new shipments arrive.
 *  - All metric maths lives in ShipmentMetricsCalculator so headline figures,
 *    per-scope breakdowns and carrier scorecards cannot disagree.
 */
@Service
@Slf4j
public class LogisticsReportServiceImpl implements LogisticsReportService {

    private static final DateTimeFormatter MONTH_KEY = DateTimeFormatter.ofPattern("yyyy-MM");

    private final LogisticsReportRepository reportRepository;
    private final ShipmentClient shipmentClient;
    private final FreightOrderClient freightOrderClient;
    private final CarrierClient carrierClient;
    private final ObjectMapper objectMapper;

    public LogisticsReportServiceImpl(LogisticsReportRepository reportRepository,
                                      ShipmentClient shipmentClient,
                                      FreightOrderClient freightOrderClient,
                                      CarrierClient carrierClient,
                                      ObjectMapper objectMapper) {
        this.reportRepository = reportRepository;
        this.shipmentClient = shipmentClient;
        this.freightOrderClient = freightOrderClient;
        this.carrierClient = carrierClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LogisticsReportDTO generateReport(ReportRequestDTO req) {
        ReportRequestDTO request = req != null ? req : new ReportRequestDTO();

        LocalDate fromDate = request.getFromDate();
        LocalDate toDate = request.getToDate();

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BadRequestException(
                    "fromDate (" + fromDate + ") cannot be after toDate (" + toDate + ").");
        }

        ReportScope scope = ReportScope.from(request.getScope());

        List<ShipmentDTO> shipments = ShipmentMetricsCalculator.filterByDispatchDate(
                shipmentClient.getAllShipments(), fromDate, toDate);

        ShipmentMetricsDTO headline = ShipmentMetricsCalculator.compute(shipments);

        // Serialised with Jackson rather than String.format: the previous
        // hand-built JSON broke as soon as a nested breakdown was added, and
        // locale-sensitive %f could emit a decimal comma and produce invalid JSON.
        Map<String, Object> metrics = new LinkedHashMap<>(
                objectMapper.convertValue(headline, new com.fasterxml.jackson.core.type.TypeReference<
                        LinkedHashMap<String, Object>>() {
                }));

        List<ScopeBreakdownDTO> breakdown = buildBreakdown(scope, shipments);

        if (!breakdown.isEmpty()) {
            metrics.put("breakdown", breakdown);
        }

        LogisticsReport report = LogisticsReport.builder()
                .scope(scope.name())
                .fromDate(fromDate)
                .toDate(toDate)
                .metrics(writeJson(metrics))
                .build();

        LogisticsReport saved = reportRepository.save(report);

        log.info("Logistics report generated: id={}, scope={}, from={}, to={}, shipmentCount={}, groups={}",
                saved.getReportId(), scope, fromDate, toDate, headline.getShipmentCount(), breakdown.size());

        return toDTO(saved);
    }

    /**
     * Groups the already-date-filtered shipments by the requested dimension.
     * GLOBAL yields no breakdown. ROUTE and HUB need freight order data, so they
     * are the only scopes that pay for that extra cross-service call.
     */
    private List<ScopeBreakdownDTO> buildBreakdown(ReportScope scope, List<ShipmentDTO> shipments) {
        if (scope == ReportScope.GLOBAL || shipments.isEmpty()) {
            return List.of();
        }

        switch (scope) {
            case CARRIER:
                return groupBreakdown(shipments,
                        s -> s.getCarrierId() != null ? String.valueOf(s.getCarrierId()) : null,
                        buildCarrierLabels());

            case PERIOD:
                return groupBreakdown(shipments,
                        s -> s.getDispatchDate() != null ? s.getDispatchDate().format(MONTH_KEY) : null,
                        Map.of());

            case ROUTE: {
                Map<Integer, FreightOrderDTO> orders = freightOrdersById();
                return groupBreakdown(shipments, s -> {
                    FreightOrderDTO order = orders.get(s.getFreightOrderId());
                    return order != null && order.getRouteId() != null
                            ? String.valueOf(order.getRouteId())
                            : null;
                }, Map.of());
            }

            case HUB: {
                Map<Integer, FreightOrderDTO> orders = freightOrdersById();
                return groupBreakdown(shipments, s -> {
                    FreightOrderDTO order = orders.get(s.getFreightOrderId());

                    if (order == null || order.getOriginLocationId() == null
                            || order.getDestinationLocationId() == null) {
                        return null;
                    }

                    return order.getOriginLocationId() + "-" + order.getDestinationLocationId();
                }, Map.of());
            }

            default:
                return List.of();
        }
    }

    /**
     * Shipments that cannot be placed in the requested dimension (no carrier, no
     * linked route, …) are dropped from the breakdown rather than lumped into a
     * misleading "unknown" bucket — the headline totals still account for them.
     */
    private List<ScopeBreakdownDTO> groupBreakdown(List<ShipmentDTO> shipments,
                                                   Function<ShipmentDTO, String> keyFn,
                                                   Map<String, String> labels) {
        Map<String, List<ShipmentDTO>> grouped = new LinkedHashMap<>();

        for (ShipmentDTO shipment : shipments) {
            String key = keyFn.apply(shipment);

            if (key != null) {
                grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(shipment);
            }
        }

        return grouped.entrySet().stream()
                .map(entry -> ScopeBreakdownDTO.builder()
                        .key(entry.getKey())
                        .label(labels.getOrDefault(entry.getKey(), entry.getKey()))
                        .metrics(ShipmentMetricsCalculator.compute(entry.getValue()))
                        .build())
                .sorted(Comparator.comparing(ScopeBreakdownDTO::getLabel))
                .collect(Collectors.toList());
    }

    private Map<Integer, FreightOrderDTO> freightOrdersById() {
        return freightOrderClient.getAllFreightOrders().stream()
                .filter(order -> order.getFreightOrderId() != null)
                .collect(Collectors.toMap(FreightOrderDTO::getFreightOrderId, Function.identity(),
                        (first, second) -> first));
    }

    /** carrierId -> "Name (#id)". Empty when carrier data is unavailable. */
    private Map<String, String> buildCarrierLabels() {
        return carriersById().values().stream()
                .collect(Collectors.toMap(
                        carrier -> String.valueOf(carrier.getCarrierId()),
                        carrier -> carrier.getName() + " (#" + carrier.getCarrierId() + ")",
                        (first, second) -> first));
    }

    private Map<Integer, CarrierDTO> carriersById() {
        return carrierClient.getAllCarriers().stream()
                .filter(carrier -> carrier.getCarrierId() != null)
                .collect(Collectors.toMap(CarrierDTO::getCarrierId, Function.identity(),
                        (first, second) -> first));
    }

    @Override
    public List<CarrierScorecardDTO> getCarrierScorecards(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BadRequestException(
                    "fromDate (" + fromDate + ") cannot be after toDate (" + toDate + ").");
        }

        List<ShipmentDTO> shipments = ShipmentMetricsCalculator.filterByDispatchDate(
                shipmentClient.getAllShipments(), fromDate, toDate);

        Map<Integer, CarrierDTO> carriers = carriersById();

        Map<Integer, List<ShipmentDTO>> byCarrier = shipments.stream()
                .filter(s -> s.getCarrierId() != null)
                .collect(Collectors.groupingBy(ShipmentDTO::getCarrierId));

        return byCarrier.entrySet().stream()
                .map(entry -> {
                    Integer carrierId = entry.getKey();
                    List<ShipmentDTO> carrierShipments = entry.getValue();
                    ShipmentMetricsDTO metrics = ShipmentMetricsCalculator.compute(carrierShipments);
                    CarrierDTO carrier = carriers.get(carrierId);

                    BigDecimal total = metrics.getTotalFreightCost() != null
                            ? metrics.getTotalFreightCost()
                            : BigDecimal.ZERO;

                    BigDecimal average = metrics.getShipmentCount() > 0
                            ? total.divide(BigDecimal.valueOf(metrics.getShipmentCount()),
                                    2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

                    return CarrierScorecardDTO.builder()
                            .carrierId(carrierId)
                            .carrierName(carrier != null ? carrier.getName() : "Carrier #" + carrierId)
                            .serviceLevel(carrier != null && carrier.getServiceLevel() != null
                                    ? carrier.getServiceLevel().name() : null)
                            .status(carrier != null && carrier.getStatus() != null
                                    ? carrier.getStatus().name() : null)
                            .shipmentCount(metrics.getShipmentCount())
                            .deliveredCount(metrics.getDeliveredCount())
                            .exceptionCount(metrics.getExceptionCount())
                            .onTimeRate(metrics.getOnTimeRate())
                            .avgTransitDays(metrics.getAvgTransitDays())
                            .exceptionRate(metrics.getExceptionRate())
                            .totalFreightCost(total)
                            .avgFreightCost(average)
                            .build();
                })
                // Worst on-time rate first: a scorecard exists to surface the
                // carrier that needs attention, not to congratulate the best one.
                .sorted(Comparator.comparingDouble(CarrierScorecardDTO::getOnTimeRate)
                        .thenComparing(CarrierScorecardDTO::getCarrierName))
                .collect(Collectors.toList());
    }

    @Override
    public List<LogisticsReportDTO> getAllReports() {
        // Newest report first — the history table is read top-down.
        return reportRepository.findAll().stream()
                .sorted(Comparator.comparing(LogisticsReport::getReportId,
                        Comparator.nullsLast(Comparator.<Integer>reverseOrder())))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LogisticsReportDTO getReportById(Integer id) {
        LogisticsReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        return toDTO(report);
    }

    /**
     * Returns the newest stored snapshot when one exists (cheap, no cross-service
     * call), and otherwise falls back to a live query so a brand-new system still
     * shows real numbers instead of an empty card.
     */
    @Override
    public Map<String, Object> getSummary() {
        List<LogisticsReport> reports = reportRepository.findAll();
        Map<String, Object> summary = new HashMap<>();

        if (!reports.isEmpty()) {
            LogisticsReport latest = reports.stream()
                    .max(Comparator.comparing(LogisticsReport::getGeneratedDate,
                            Comparator.nullsFirst(Comparator.naturalOrder())))
                    .orElse(reports.get(reports.size() - 1));
            summary.put("reportId", latest.getReportId());
            summary.put("scope", latest.getScope());
            summary.put("fromDate", latest.getFromDate());
            summary.put("toDate", latest.getToDate());
            summary.put("metrics", latest.getMetrics());
            summary.put("generatedDate", latest.getGeneratedDate());
            return summary;
        }

        ShipmentMetricsDTO live = ShipmentMetricsCalculator.compute(shipmentClient.getAllShipments());
        summary.put("scope", ReportScope.GLOBAL.name());
        summary.put("metrics", writeJson(live));
        return summary;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            // Should be unreachable for plain DTOs/maps, but a report must never
            // be persisted with a half-written metrics blob.
            log.error("Failed to serialise report metrics: {}", e.getMessage());
            throw new IllegalStateException("Could not serialise report metrics", e);
        }
    }

    private LogisticsReportDTO toDTO(LogisticsReport report) {
        return LogisticsReportDTO.builder()
                .reportId(report.getReportId())
                .scope(report.getScope())
                .fromDate(report.getFromDate())
                .toDate(report.getToDate())
                .metrics(report.getMetrics())
                .generatedDate(report.getGeneratedDate())
                .build();
    }
}
