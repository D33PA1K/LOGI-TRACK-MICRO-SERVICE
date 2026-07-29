package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.ComplianceFlagDTO;
import com.cognizant.logitrack.enums.FlagStatus;
import java.util.List;

public interface ComplianceFlagService {
    ComplianceFlagDTO raiseFlag(ComplianceFlagDTO dto);
    ComplianceFlagDTO resolveFlag(Integer id);
    List<ComplianceFlagDTO> getFlagsByShipment(Integer shipmentId);
    List<ComplianceFlagDTO> getOpenFlags();
    ComplianceFlagDTO getById(Integer id);
	List<ComplianceFlagDTO> getResolvedFlags();
    List<ComplianceFlagDTO> getFlagsByStatus(FlagStatus status);
    List<ComplianceFlagDTO> getFlagsByShipmentAndStatus(Integer shipmentId, FlagStatus status);
}
