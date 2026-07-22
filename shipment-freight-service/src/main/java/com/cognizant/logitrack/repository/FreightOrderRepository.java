package com.cognizant.logitrack.repository;

import com.cognizant.logitrack.entity.FreightOrder;
import com.cognizant.logitrack.enums.FreightOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FreightOrderRepository extends JpaRepository<FreightOrder, Integer> {
    List<FreightOrder> findByShipperId(Integer shipperId);
    List<FreightOrder> findByStatus(FreightOrderStatus status);
}
