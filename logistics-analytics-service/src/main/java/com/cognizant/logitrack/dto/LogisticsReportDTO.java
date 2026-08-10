package com.cognizant.logitrack.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsReportDTO {
    private Integer reportId;
    private String scope;

    /** The period the report covers; null on either side means unbounded. */
    private LocalDate fromDate;
    private LocalDate toDate;

    /** Metrics as a JSON string, including a "breakdown" array for scoped reports. */
    private String metrics;

    private LocalDateTime generatedDate;
}
