package com.cognizant.logitrack.entity;

import com.cognizant.logitrack.enums.EventType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "delivery_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer eventId;
    @ManyToOne
    @JoinColumn(name = "ShipmentID")
    private Shipment shipment;
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    @CreationTimestamp
    private LocalDateTime timestamp;
    private Integer locationId;
    @Column(columnDefinition = "TEXT")
    private String notes;
}
