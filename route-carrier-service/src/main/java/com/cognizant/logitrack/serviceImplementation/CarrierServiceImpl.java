package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.service.CarrierService;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.dto.CarrierDTO;
import com.cognizant.logitrack.entity.Carrier;
import com.cognizant.logitrack.enums.CarrierStatus;
import com.cognizant.logitrack.enums.RouteMode;
import com.cognizant.logitrack.repository.CarrierRepository;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CarrierServiceImpl implements CarrierService {
    private final CarrierRepository carrierRepository;

    public CarrierServiceImpl(CarrierRepository carrierRepository) {
        this.carrierRepository = carrierRepository;
    }

    @Override
    public CarrierDTO addCarrier(CarrierDTO dto) {
        Carrier carrier = Carrier.builder().name(dto.getName()).mode(dto.getMode()).serviceLevel(dto.getServiceLevel()).contactDetails(dto.getContactDetails()).status(CarrierStatus.ACTIVE).build();
        Carrier saved = carrierRepository.save(carrier);
        log.info("Carrier added: id={}, name={}", saved.getCarrierId(), saved.getName());
        return toDTO(saved);
    }

    @Override
    public List<CarrierDTO> getAllCarriers() {
        return carrierRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CarrierDTO> getByMode(RouteMode mode) {
        return carrierRepository.findByMode(mode).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public CarrierDTO getById(Integer id) {
        return toDTO(findEntity(id));
    }

    @Override
    public CarrierDTO updateCarrierStatus(Integer id, CarrierStatus status) {
        Carrier carrier = findEntity(id);
        carrier.setStatus(status);
        return toDTO(carrierRepository.save(carrier));
    }

    @Override
    public void deleteCarrier(Integer id) {
        Carrier carrier = findEntity(id);
        carrier.setStatus(CarrierStatus.SUSPENDED);
        carrierRepository.save(carrier);
        log.info("Carrier soft-deleted (suspended): id={}", id);
    }

    private Carrier findEntity(Integer id) {
        return carrierRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Carrier not found with id: " + id));
    }

    private CarrierDTO toDTO(Carrier carrier) {
        return CarrierDTO.builder().carrierId(carrier.getCarrierId()).name(carrier.getName()).mode(carrier.getMode()).serviceLevel(carrier.getServiceLevel()).contactDetails(carrier.getContactDetails()).status(carrier.getStatus()).build();
    }
}
