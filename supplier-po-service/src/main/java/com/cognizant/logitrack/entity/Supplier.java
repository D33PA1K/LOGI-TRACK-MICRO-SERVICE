package com.cognizant.logitrack.entity;

import com.cognizant.logitrack.enums.SupplierStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "suppliers", uniqueConstraints = @UniqueConstraint(
        name = "uk_supplier_name_category_contact",
        columnNames = {"name", "category", "contact_details"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer supplierId;
    // Bounded lengths (VARCHAR, not TEXT) so the three columns can participate in
    // a composite unique index within MySQL's index-length limit. This is the
    // authoritative, race-safe guard against duplicate suppliers; the service
    // also pre-checks so the common case returns a friendly message.
    @Column(name = "name", length = 255)
    private String name;
    @Column(name = "category", length = 150)
    private String category;
    @Column(name = "contact_details", length = 255)
    private String contactDetails;
    private Integer leadTimeDays;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SupplierStatus status = SupplierStatus.ACTIVE;
}
