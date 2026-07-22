package com.cognizant.logitrack.service;

import com.cognizant.logitrack.dto.ComplianceFlagDTO;
import java.util.List;

public interface ComplianceFlagService {
    ComplianceFlagDTO raiseFlag(ComplianceFlagDTO dto);
    ComplianceFlagDTO resolveFlag(Integer id);
    List<ComplianceFlagDTO> getFlagsByShipment(Integer shipmentId);
    List<ComplianceFlagDTO> getOpenFlags();
    ComplianceFlagDTO getById(Integer id);
	List<ComplianceFlagDTO> getResolvedFlags();
}
