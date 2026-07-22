package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.PickListDTO;
import com.cognizant.logitrack.enums.PickListStatus;
import java.util.List;

public interface PickListService {
    PickListDTO createPickList(PickListDTO dto);
    PickListDTO assignPickList(Integer id, Integer assignedTo);
    PickListDTO updatePickListStatus(Integer id, PickListStatus status);
    List<PickListDTO> getByWarehouse(Integer warehouseId);
    List<PickListDTO> getByAssignedUser(Integer userId);
}
