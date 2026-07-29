package com.cognizant.logitrack.serviceImplementation;
 
import com.cognizant.logitrack.service.ComplianceFlagService;
import com.cognizant.logitrack.exception.BadRequestException;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import com.cognizant.logitrack.client.ShipmentClient;
import com.cognizant.logitrack.dto.ComplianceFlagDTO;
import com.cognizant.logitrack.dto.ShipmentDTO;
import com.cognizant.logitrack.entity.ComplianceFlag;
import com.cognizant.logitrack.enums.FlagStatus;
import com.cognizant.logitrack.repository.ComplianceFlagRepository;
 
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;
 
@Service
@Slf4j
public class ComplianceFlagServiceImpl implements ComplianceFlagService {
    private final ComplianceFlagRepository flagRepository;
    private final ShipmentClient shipmentClient;
     
    public ComplianceFlagServiceImpl(ComplianceFlagRepository flagRepository, ShipmentClient shipmentClient) {
        this.flagRepository = flagRepository;
        this.shipmentClient = shipmentClient;
    }
 
    @Override
    public ComplianceFlagDTO raiseFlag(ComplianceFlagDTO dto) {
        ShipmentDTO shipment = shipmentClient.getShipmentById(dto.getShipmentId());
        if (shipment == null) {
            throw new BadRequestException("Shipment does not exist or service is unavailable");
        }
        
    	ComplianceFlag flag = ComplianceFlag.builder().shipmentId(dto.getShipmentId()).flagType(dto.getFlagType()).severity(dto.getSeverity()).status(FlagStatus.OPEN).build();
        ComplianceFlag saved = flagRepository.save(flag);
        log.info("Compliance flag raised: id={}, shipmentId={}", saved.getFlagId(), saved.getShipmentId());
        return toDTO(saved);
    }
 
    @Override
    public ComplianceFlagDTO resolveFlag(Integer id) {
        ComplianceFlag flag = findEntity(id);
        flag.setStatus(FlagStatus.RESOLVED);
        log.info("Compliance flag {} resolved", id);
        return toDTO(flagRepository.save(flag));
    }
 
    @Override
    public List<ComplianceFlagDTO> getFlagsByShipment(Integer shipmentId) {
        return flagRepository.findByShipmentId(shipmentId).stream().map(this::toDTO).collect(Collectors.toList());
    }
 
    @Override
    public List<ComplianceFlagDTO> getOpenFlags() {
        return flagRepository.findByStatus(FlagStatus.OPEN).stream().map(this::toDTO).collect(Collectors.toList());
    }
    
	@Override
	public List<ComplianceFlagDTO> getResolvedFlags() {
		return flagRepository.findByStatus(FlagStatus.RESOLVED).stream().map(this::toDTO).collect(Collectors.toList());
	}

    @Override
    public List<ComplianceFlagDTO> getFlagsByStatus(FlagStatus status) {
        return flagRepository.findByStatus(status).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ComplianceFlagDTO> getFlagsByShipmentAndStatus(Integer shipmentId, FlagStatus status) {
        return flagRepository.findByShipmentIdAndStatus(shipmentId, status).stream().map(this::toDTO).collect(Collectors.toList());
    }
 
    @Override
    public ComplianceFlagDTO getById(Integer id) {
        return toDTO(findEntity(id));
    }
 
    private ComplianceFlag findEntity(Integer id) {
        return flagRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Compliance flag not found with id: " + id));
    }
 
    private ComplianceFlagDTO toDTO(ComplianceFlag f) {
        return ComplianceFlagDTO.builder().flagId(f.getFlagId()).shipmentId(f.getShipmentId()).flagType(f.getFlagType()).severity(f.getSeverity()).raisedDate(f.getRaisedDate()).status(f.getStatus()).build();
    }
 
 
}

