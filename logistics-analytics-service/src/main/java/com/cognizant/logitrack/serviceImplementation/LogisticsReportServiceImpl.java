package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.service.LogisticsReportService;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.LogisticsReportDTO;
import com.cognizant.logitrack.dto.ReportRequestDTO;
import com.cognizant.logitrack.dto.ShipmentDTO;
import com.cognizant.logitrack.client.ShipmentClient;
import com.cognizant.logitrack.entity.LogisticsReport;
import com.cognizant.logitrack.repository.LogisticsReportRepository;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LogisticsReportServiceImpl implements LogisticsReportService {

    private final LogisticsReportRepository reportRepository;
    private final ShipmentClient shipmentClient;

    public LogisticsReportServiceImpl(LogisticsReportRepository reportRepository, ShipmentClient shipmentClient) {
        this.reportRepository = reportRepository;
        this.shipmentClient = shipmentClient;
    }

    /**
     * Gap 12 fix: metrics are now computed from real Shipment data instead of hardcoded values.
     *
     * Metrics computed:
     *  - shipmentCount     : total number of shipments
     *  - deliveredCount    : number of DELIVERED shipments
     *  - exceptionCount    : number of EXCEPTION or DELAYED shipments
     *  - onTimeRate        : % of delivered shipments where actualArrival <= estimatedArrival
     *  - avgTransitDays    : average (actualArrival - dispatchDate) for DELIVERED shipments
     *  - totalFreightCost  : sum of freightCost across all shipments
     *  - exceptionRate     : % of total shipments that are EXCEPTION or DELAYED
     */
    @Override
    public LogisticsReportDTO generateReport(ReportRequestDTO req) {
        List<ShipmentDTO> all = shipmentClient.getAllShipments();

        long shipmentCount  = all.size();
        long deliveredCount = all.stream().filter(s -> "DELIVERED".equals(s.getStatus())).count();
        long exceptionCount = all.stream().filter(s ->
                "EXCEPTION".equals(s.getStatus()) || "DELAYED".equals(s.getStatus())).count();

        List<ShipmentDTO> delivered = all.stream()
                .filter(s -> "DELIVERED".equals(s.getStatus())
                        && s.getActualArrival() != null
                        && s.getEstimatedArrival() != null)
                .collect(Collectors.toList());

        long onTimeCount = delivered.stream()
                .filter(s -> !s.getActualArrival().isAfter(s.getEstimatedArrival()))
                .count();

        double onTimeRate = delivered.isEmpty() ? 0.0
                : BigDecimal.valueOf((double) onTimeCount / delivered.size() * 100)
                        .setScale(1, RoundingMode.HALF_UP).doubleValue();

        double avgTransitDays = delivered.stream()
                .filter(s -> s.getDispatchDate() != null)
                .mapToLong(s -> s.getDispatchDate().until(s.getActualArrival(),
                        java.time.temporal.ChronoUnit.DAYS))
                .average()
                .orElse(0.0);
        avgTransitDays = BigDecimal.valueOf(avgTransitDays)
                .setScale(1, RoundingMode.HALF_UP).doubleValue();

        BigDecimal totalFreightCost = all.stream()
                .filter(s -> s.getFreightCost() != null)
                .map(ShipmentDTO::getFreightCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double exceptionRate = shipmentCount == 0 ? 0.0
                : BigDecimal.valueOf((double) exceptionCount / shipmentCount * 100)
                        .setScale(1, RoundingMode.HALF_UP).doubleValue();

        String metrics = String.format(
                "{\"shipmentCount\":%d,\"deliveredCount\":%d,\"exceptionCount\":%d,"
                        + "\"onTimeRate\":%.1f,\"avgTransitDays\":%.1f,"
                        + "\"totalFreightCost\":%.2f,\"exceptionRate\":%.1f}",
                shipmentCount, deliveredCount, exceptionCount,
                onTimeRate, avgTransitDays,
                totalFreightCost, exceptionRate
        );

        String scope = (req.getScope() != null && !req.getScope().isBlank()) ? req.getScope() : "GLOBAL";

        LogisticsReport report = LogisticsReport.builder()
                .scope(scope)
                .metrics(metrics)
                .build();

        LogisticsReport saved = reportRepository.save(report);
        log.info("Logistics report generated: id={}, scope={}, shipmentCount={}", saved.getReportId(), scope, shipmentCount);

        return toDTO(saved);
    }

    @Override
    public List<LogisticsReportDTO> getAllReports() {
        return reportRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public LogisticsReportDTO getReportById(Integer id) {
        LogisticsReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        return toDTO(report);
    }

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
            summary.put("metrics", latest.getMetrics());
            summary.put("generatedDate", latest.getGeneratedDate());
        } else {
            List<ShipmentDTO> all = shipmentClient.getAllShipments();
            long total = all.size();
            long delivered = all.stream().filter(s -> "DELIVERED".equals(s.getStatus())).count();
            BigDecimal cost = all.stream().filter(s -> s.getFreightCost() != null)
                    .map(ShipmentDTO::getFreightCost).reduce(BigDecimal.ZERO, BigDecimal::add);
            summary.put("totalShipments", total);
            summary.put("deliveredShipments", delivered);
            summary.put("totalFreightCost", cost);
            summary.put("onTimeRate", 0.0);
        }
        return summary;
    }

    private LogisticsReportDTO toDTO(LogisticsReport report) {
        return LogisticsReportDTO.builder()
                .reportId(report.getReportId())
                .scope(report.getScope())
                .metrics(report.getMetrics())
                .generatedDate(report.getGeneratedDate())
                .build();
    }
}
