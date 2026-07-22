package com.cognizant.logitrack.repository;

import com.cognizant.logitrack.entity.LogisticsReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogisticsReportRepository extends JpaRepository<LogisticsReport, Integer> {
    List<LogisticsReport> findByScope(String scope);
}
