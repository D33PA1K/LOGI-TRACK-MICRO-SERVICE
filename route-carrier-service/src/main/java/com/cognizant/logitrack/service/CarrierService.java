package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.CarrierDTO;
import com.cognizant.logitrack.enums.CarrierStatus;
import com.cognizant.logitrack.enums.RouteMode;
import java.util.List;

public interface CarrierService {
    CarrierDTO addCarrier(CarrierDTO dto);
    List<CarrierDTO> getAllCarriers();
    List<CarrierDTO> getByMode(RouteMode mode);
    CarrierDTO getById(Integer id);
    CarrierDTO updateCarrierStatus(Integer id, CarrierStatus status);
    void deleteCarrier(Integer id);
}
