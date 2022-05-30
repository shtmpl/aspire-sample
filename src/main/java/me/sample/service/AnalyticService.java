package me.sample.service;

import me.sample.enumeration.TimePeriod;
import me.sample.domain.AnalyticReport;
import me.sample.domain.PosFrequency;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AnalyticService {

    String REPORT_NAME_AGE = "age";
    String REPORT_NAME_CITY = "city";
    String REPORT_NAME_VENDOR = "vendor";


    List<PosFrequency> getPosFrequency(TimePeriod timePeriod, LocalDateTime from, LocalDateTime to);

    Optional<AnalyticReport> findAnalyticReport(String reportName, TimePeriod timePeriod);

    Optional<AnalyticReport> getVendorPercentages(TimePeriod timePeriod);

    Optional<AnalyticReport> getAgePercentages(TimePeriod timePeriod);

    Optional<AnalyticReport> getCityPercentages(TimePeriod timePeriod);

    List<Map<String, Object>> mergeAnalyticReportData(String reportName, Collection<List<Map<String, Object>>> data);

    void generateAndSaveAnalyticReports();
}
