package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.CarrierScorecardDTO;
import com.cognizant.logitrack.dto.LogisticsReportDTO;
import com.cognizant.logitrack.dto.ReportRequestDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface LogisticsReportService {
    LogisticsReportDTO generateReport(ReportRequestDTO req);
    List<LogisticsReportDTO> getAllReports();
    LogisticsReportDTO getReportById(Integer id);
    Map<String, Object> getSummary();

    /** Per-carrier performance, ranked worst on-time rate first. */
    List<CarrierScorecardDTO> getCarrierScorecards(LocalDate fromDate, LocalDate toDate);
}
