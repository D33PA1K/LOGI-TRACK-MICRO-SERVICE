package com.cognizant.logitrack.entity;

import com.cognizant.logitrack.enums.CarrierServiceLevel;
import com.cognizant.logitrack.enums.CarrierStatus;
import com.cognizant.logitrack.enums.RouteMode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carriers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Carrier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer carrierId;
    private String name;
    @Enumerated(EnumType.STRING)
    private RouteMode mode;
    @Enumerated(EnumType.STRING)
    private CarrierServiceLevel serviceLevel;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String contactDetails;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CarrierStatus status = CarrierStatus.ACTIVE;
}
