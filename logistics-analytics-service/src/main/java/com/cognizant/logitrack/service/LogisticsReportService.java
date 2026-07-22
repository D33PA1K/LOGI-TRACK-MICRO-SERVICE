package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.LogisticsReportDTO;
import com.cognizant.logitrack.dto.ReportRequestDTO;
import java.util.List;
import java.util.Map;

public interface LogisticsReportService {
    LogisticsReportDTO generateReport(ReportRequestDTO req);
    List<LogisticsReportDTO> getAllReports();
    LogisticsReportDTO getReportById(Integer id);
    Map<String, Object> getSummary();
}
