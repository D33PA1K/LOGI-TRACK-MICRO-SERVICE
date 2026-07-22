package com.cognizant.logitrack.dto;

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
    private String metrics;
    private LocalDateTime generatedDate;
}
