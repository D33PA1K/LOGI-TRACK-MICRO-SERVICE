package com.cognizant.logitrack.dto;

import com.cognizant.logitrack.enums.EventType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEventDTO {
    private Integer eventId;
    private Integer shipmentId;
    @NotNull
    private EventType eventType;
    private LocalDateTime timestamp;
    private Integer locationId;
    private String notes;
}
