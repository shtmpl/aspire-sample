package me.sample.repository;

import me.sample.enumeration.TimePeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import me.sample.domain.AnalyticReport;

import java.util.Optional;
import java.util.UUID;

public interface AnalyticReportRepository extends JpaRepository<AnalyticReport, UUID> {

    Optional<AnalyticReport> findFirstByCompanyIdAndNameAndTimePeriodOrderByUpdatedDateDesc(UUID companyId,
                                                                                            String name,
                                                                                            TimePeriod timePeriod);
}
