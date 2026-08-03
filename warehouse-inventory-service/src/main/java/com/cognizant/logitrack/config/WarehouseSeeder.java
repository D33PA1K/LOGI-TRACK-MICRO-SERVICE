package com.cognizant.logitrack.config;

import com.cognizant.logitrack.entity.Warehouse;
import com.cognizant.logitrack.enums.WarehouseStatus;
import com.cognizant.logitrack.repository.WarehouseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Warehouse management is out of scope for this project, so instead of a full
 * CRUD flow we seed a fixed set of 10 sample warehouses on startup. Seeding runs
 * only when the table is empty, so restarts don't create duplicates and the
 * generated ids stay stable at 1..10 (which existing warehouseId references and
 * the seed data across services already assume).
 */
@Configuration
@Slf4j
public class WarehouseSeeder {

    @Bean
    public CommandLineRunner seedWarehouses(WarehouseRepository warehouseRepository) {
        return args -> {
            if (warehouseRepository.count() > 0) {
                log.info("Warehouses already present ({}); skipping seed.", warehouseRepository.count());
                return;
            }

            List<Warehouse> warehouses = List.of(
                    build("Chennai Central DC", "12 Mount Road, Guindy", "Chennai", "Tamil Nadu", "India", "600032", "+91-44-2345-6789"),
                    build("Mumbai West Hub", "88 Andheri Kurla Road", "Mumbai", "Maharashtra", "India", "400059", "+91-22-6789-1234"),
                    build("Delhi North DC", "Plot 7, Okhla Industrial Area", "New Delhi", "Delhi", "India", "110020", "+91-11-4567-8901"),
                    build("Bengaluru Tech Park WH", "45 Whitefield Main Road", "Bengaluru", "Karnataka", "India", "560066", "+91-80-2345-6780"),
                    build("Hyderabad South DC", "23 Shamshabad Logistics Zone", "Hyderabad", "Telangana", "India", "501218", "+91-40-3456-7891"),
                    build("Kolkata East Hub", "9 Salt Lake Sector V", "Kolkata", "West Bengal", "India", "700091", "+91-33-4567-8902"),
                    build("Pune Ranjangaon WH", "Building C, MIDC Ranjangaon", "Pune", "Maharashtra", "India", "412220", "+91-20-6789-2345"),
                    build("Ahmedabad Sanand DC", "SEZ Road, Sanand GIDC", "Ahmedabad", "Gujarat", "India", "382110", "+91-79-2345-6781"),
                    build("Singapore Regional DC", "5 Changi North Way", "Singapore", "Singapore", "Singapore", "498771", "+65-6543-2100"),
                    build("Dubai Jebel Ali WH", "Warehouse 14, Jebel Ali Free Zone", "Dubai", "Dubai", "United Arab Emirates", "17000", "+971-4-887-1234")
            );

            warehouseRepository.saveAll(warehouses);
            log.info("Seeded {} sample warehouses.", warehouses.size());
        };
    }

    private Warehouse build(String name, String addressLine, String city, String state, String country,
                            String postalCode, String contactNumber) {
        return Warehouse.builder()
                .warehouseName(name)
                .addressLine(addressLine)
                .city(city)
                .state(state)
                .country(country)
                .postalCode(postalCode)
                .contactNumber(contactNumber)
                .status(WarehouseStatus.ACTIVE)
                .build();
    }
}
