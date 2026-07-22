package com.cognizant.logitrack.entity;

import com.cognizant.logitrack.enums.PickListStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pick_lists")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pickListId;
    private Integer freightOrderId;
    private Integer warehouseId;
    private Integer assignedTo;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PickListStatus status = PickListStatus.OPEN;
    @CreationTimestamp
    private LocalDate createdDate;
}
