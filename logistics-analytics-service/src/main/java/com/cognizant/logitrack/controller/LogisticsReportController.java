package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.CarrierScorecardDTO;
import com.cognizant.logitrack.dto.LogisticsReportDTO;
import com.cognizant.logitrack.dto.ReportRequestDTO;
import com.cognizant.logitrack.service.LogisticsReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Every endpoint here is restricted to ANALYST and ADMIN by SecurityConfig,
 * including POST: generating a report triggers a cross-service aggregation, so
 * it is as privileged as reading one.
 */
@RestController
@RequestMapping("/api/logistics-reports")
public class LogisticsReportController {

    private final LogisticsReportService reportService;

    public LogisticsReportController(LogisticsReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<LogisticsReportDTO> generateReport(@RequestBody(required = false) ReportRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.generateReport(req));
    }

    @GetMapping
    public ResponseEntity<List<LogisticsReportDTO>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(reportService.getSummary());
    }

    /**
     * Carrier scorecards, computed live rather than from a stored snapshot so the
     * ranking always reflects current performance. Declared before /{id} so
     * "carrier-scorecards" is never parsed as a report id.
     */
    @GetMapping("/carrier-scorecards")
    public ResponseEntity<List<CarrierScorecardDTO>> getCarrierScorecards(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(reportService.getCarrierScorecards(fromDate, toDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LogisticsReportDTO> getReportById(@PathVariable Integer id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }
}
