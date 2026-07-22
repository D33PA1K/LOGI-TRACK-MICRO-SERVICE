package com.cognizant.logitrack.repository;

import com.cognizant.logitrack.entity.InboundReceipt;
import com.cognizant.logitrack.enums.ReceiptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InboundReceiptRepository extends JpaRepository<InboundReceipt, Integer> {

    List<InboundReceipt> findByWarehouseId(Integer warehouseId);

    List<InboundReceipt> findByStatus(ReceiptStatus status);
}