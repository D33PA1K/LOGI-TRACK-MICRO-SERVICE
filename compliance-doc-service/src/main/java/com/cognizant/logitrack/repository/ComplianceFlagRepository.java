package com.cognizant.logitrack.repository;
 
import com.cognizant.logitrack.entity.ComplianceFlag;
import com.cognizant.logitrack.enums.FlagSeverity;
import com.cognizant.logitrack.enums.FlagStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
 
@Repository
public interface ComplianceFlagRepository extends JpaRepository<ComplianceFlag, Integer> {
    List<ComplianceFlag> findByShipmentId(Integer shipmentId);
    List<ComplianceFlag> findByStatus(FlagStatus status);
    List<ComplianceFlag> findBySeverity(FlagSeverity severity);
}
