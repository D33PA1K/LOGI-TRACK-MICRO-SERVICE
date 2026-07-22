package com.cognizant.logitrack.controller;

import com.cognizant.logitrack.dto.LogisticsReportDTO;
import com.cognizant.logitrack.dto.ReportRequestDTO;
import com.cognizant.logitrack.service.LogisticsReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logistics-reports")
public class LogisticsReportController {

    private final LogisticsReportService reportService;

    public LogisticsReportController(LogisticsReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<LogisticsReportDTO> generateReport(@RequestBody ReportRequestDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.generateReport(req));
    }

    @GetMapping
    public ResponseEntity<List<LogisticsReportDTO>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LogisticsReportDTO> getReportById(@PathVariable Integer id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(reportService.getSummary());
    }
}

