package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.FreightOrderDTO;
import com.cognizant.logitrack.enums.FreightOrderStatus;
import java.util.List;

public interface FreightOrderService {
    FreightOrderDTO createFreightOrder(FreightOrderDTO dto);
    FreightOrderDTO getById(Integer id);
    List<FreightOrderDTO> getAllOrders();
    List<FreightOrderDTO> getByShipper(Integer shipperId);
    FreightOrderDTO updateStatus(Integer id, FreightOrderStatus status);
    FreightOrderDTO cancelOrder(Integer id);
}
