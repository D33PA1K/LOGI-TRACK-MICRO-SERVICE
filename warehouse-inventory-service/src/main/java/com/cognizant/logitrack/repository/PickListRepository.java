package com.cognizant.logitrack.repository;

import com.cognizant.logitrack.entity.PickList;
import com.cognizant.logitrack.enums.PickListStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PickListRepository extends JpaRepository<PickList, Integer> {
    List<PickList> findByFreightOrderId(Integer freightOrderId);
    List<PickList> findByAssignedTo(Integer assignedTo);
    List<PickList> findByStatus(PickListStatus status);
    List<PickList> findByWarehouseId(Integer warehouseId);
}
