package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.enumeration.TimePeriod;
import me.sample.domain.AccumulatedAnalyticReport;
import me.sample.domain.AgePercentage;
import me.sample.domain.AnalyticReport;
import me.sample.domain.Cities;
import me.sample.domain.CityPercentage;
import me.sample.domain.Company;
import me.sample.domain.CompanyAuthority;
import me.sample.domain.PosFrequency;
import me.sample.domain.Terminal;
import me.sample.domain.VendorPercentage;
import me.sample.repository.CompanyAuthorityRepository;
import me.sample.repository.CompanyRepository;
import me.sample.repository.PosFrequencyRepository;
import me.sample.repository.TerminalRepository;
import me.sample.repository.AnalyticReportRepository;
import me.sample.service.AnalyticService;
import me.sample.service.SecurityService;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@CacheConfig(cacheNames = AnalyticServiceImpl.ANALYTIC_CACHE)
@Transactional
@Service
public class AnalyticServiceImpl implements AnalyticService {

    public static final String ANALYTIC_CACHE = "analytic";

    SecurityService securityService;
    CompanyAuthorityRepository companyAuthorityRepository;

    TerminalRepository terminalRepository;

    CompanyRepository companyRepository;

    PosFrequencyRepository posFrequencyRepository;
    AnalyticReportRepository analyticReportRepository;

    @Cacheable
    @Transactional(readOnly = true)
    @Override
    public List<PosFrequency> getPosFrequency(@NonNull TimePeriod timePeriod, LocalDateTime from, LocalDateTime to) {

        switch (timePeriod) {
            case HOUR:
                return posFrequencyRepository.perHour(from, to);
            case DAY:
                return posFrequencyRepository.perDay(from, to);
            case WEEK:
                return posFrequencyRepository.perWeek(from, to);
            case MONTH:
                return posFrequencyRepository.perMonth(from, to);
            default:
                throw new UnsupportedOperationException("Unknown time period " + timePeriod.name());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<AnalyticReport> findAnalyticReport(String reportName, TimePeriod timePeriod) {
        Long userId = securityService.getUserId();
        List<UUID> companyIds = companyAuthorityRepository.findAllByUserId(userId).stream()
                .map(CompanyAuthority::getCompany)
                .map(Company::getId)
                .collect(Collectors.toList());

        List<AnalyticReport> analyticReports = companyIds.stream()
                .map((UUID companyId) ->
                        analyticReportRepository.findFirstByCompanyIdAndNameAndTimePeriodOrderByUpdatedDateDesc(
                                companyId,
                                reportName,
                                timePeriod)
                                .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (analyticReports.isEmpty()) {
            return Optional.empty();
        }

        List<List<Map<String, Object>>> analyticReportData = analyticReports.stream()
                .map(AnalyticReport::getData)
                .map((Object data) -> (List<Map<String, Object>>) data)
                .collect(Collectors.toList());

        return Optional.of(AnalyticReport.builder()
                .name(reportName)
                .timePeriod(timePeriod)
                .data(mergeAnalyticReportData(reportName, analyticReportData))
                .build());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<AnalyticReport> getAgePercentages(@NonNull TimePeriod timePeriod) {
        return findAnalyticReport(REPORT_NAME_AGE, timePeriod);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<AnalyticReport> getCityPercentages(@NonNull TimePeriod timePeriod) {
        return findAnalyticReport(REPORT_NAME_CITY, timePeriod);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<AnalyticReport> getVendorPercentages(@NonNull TimePeriod timePeriod) {
        return findAnalyticReport(REPORT_NAME_VENDOR, timePeriod);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Map<String, Object>> mergeAnalyticReportData(String reportName, Collection<List<Map<String, Object>>> data) {
        if (reportName == null || data.isEmpty()) {
            return Collections.emptyList();
        }

        switch (reportName) {
            case REPORT_NAME_AGE:
                return mergeAgeAnalyticReportData(data);
            case REPORT_NAME_CITY:
                return mergeCityAnalyticReportData(data);
            case REPORT_NAME_VENDOR:
                return mergeVendorAnalyticReportData(data);
            default:
                return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> mergeAgeAnalyticReportData(Collection<List<Map<String, Object>>> data) {
        Map<String, Long> distribution = new LinkedHashMap<>();
        data.stream()
                .flatMap(Collection::stream)
                .map(AgePercentage::fromMap)
                .forEach((AgePercentage unit) ->
                        distribution.merge(unit.getAgeRange(), unit.getCount(), Long::sum));

        long total = distribution.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        return distribution.entrySet().stream()
                .map((Map.Entry<String, Long> entry) ->
                        AgePercentage.builder()
                                .id(String.valueOf(UUID.randomUUID()))
                                .ageRange(entry.getKey())
                                .count(entry.getValue())
                                .percentage(entry.getValue() / (double) total)
                                .build())
                .map(AgePercentage::toMap)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> mergeCityAnalyticReportData(Collection<List<Map<String, Object>>> data) {
        Map<String, Long> distribution = new LinkedHashMap<>();
        data.stream()
                .flatMap(Collection::stream)
                .map(CityPercentage::fromMap)
                .forEach((CityPercentage unit) ->
                        distribution.merge(unit.getCity(), unit.getCount(), Long::sum));

        long total = distribution.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        return distribution.entrySet().stream()
                .map((Map.Entry<String, Long> entry) ->
                        CityPercentage.builder()
                                .id(String.valueOf(UUID.randomUUID()))
                                .city(entry.getKey())
                                .count(entry.getValue())
                                .percentage(entry.getValue() / (double) total)
                                .build())
                .map(CityPercentage::toMap)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> mergeVendorAnalyticReportData(Collection<List<Map<String, Object>>> data) {
        Map<String, Long> distribution = new LinkedHashMap<>();
        data.stream()
                .flatMap(Collection::stream)
                .map(VendorPercentage::fromMap)
                .forEach((VendorPercentage unit) ->
                        distribution.merge(unit.getVendor(), unit.getCount(), Long::sum));

        long total = distribution.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        return distribution.entrySet().stream()
                .map((Map.Entry<String, Long> entry) ->
                        VendorPercentage.builder()
                                .id(String.valueOf(UUID.randomUUID()))
                                .vendor(entry.getKey())
                                .count(entry.getValue())
                                .percentage(entry.getValue() / (double) total)
                                .build())
                .map(VendorPercentage::toMap)
                .collect(Collectors.toList());
    }

    @Override
    public void generateAndSaveAnalyticReports() {
        LocalDateTime now = LocalDateTime.now();

        Map<UUID, AccumulatedAnalyticReport> accumulatedAnalyticReportsByCompanyId = new LinkedHashMap<>();
        for (Company company : companyRepository.findAll()) {
            accumulatedAnalyticReportsByCompanyId.put(
                    company.getId(),
                    AccumulatedAnalyticReport.builder()
                            .build());
        }

        try (Stream<Terminal> terminals = terminalRepository.findAsStreamAll()) {
            terminals.forEach((Terminal terminal) -> {
                UUID companyId = terminal.getApplication().getCompany().getId();
                if (companyId == null) {
                    return;
                }

                AccumulatedAnalyticReport accumulatedAnalyticReport = accumulatedAnalyticReportsByCompanyId.get(companyId);


                Year birthYear = getTerminalBirthYear(terminal);
                String city = getTerminalCity(terminal);
                String vendor = getTerminalVendor(terminal);

                accumulatedAnalyticReport
                        .addTerminalBirthYearToDistribution(birthYear)
                        .addTerminalCityToDistribution(city)
                        .addTerminalVendorToDistribution(vendor);


                LocalDateTime terminalUpdatedAt = terminal.getUpdatedDate();

                if (terminalUpdatedAt.isEqual(now.minusMonths(1)) ||
                        terminalUpdatedAt.isAfter(now.minusMonths(1))) {
                    accumulatedAnalyticReport
                            .addTerminalBirthYearToLastMonthDistribution(birthYear)
                            .addTerminalCityToLastMonthDistribution(city)
                            .addTerminalVendorToLastMonthDistribution(vendor);
                }

                if (terminalUpdatedAt.isEqual(now.minusWeeks(1)) ||
                        terminalUpdatedAt.isAfter(now.minusWeeks(1))) {
                    accumulatedAnalyticReport
                            .addTerminalBirthYearToLastWeekDistribution(birthYear)
                            .addTerminalCityToLastWeekDistribution(city)
                            .addTerminalVendorToLastWeekDistribution(vendor);
                }
            });
        }

        accumulatedAnalyticReportsByCompanyId.forEach((UUID companyId, AccumulatedAnalyticReport accumulatedAnalyticReport) -> {
            analyticReportRepository.saveAll(Arrays.asList(
                    AnalyticReport.builder()
                            .companyId(companyId)
                            .name(REPORT_NAME_AGE)
                            .timePeriod(TimePeriod.ALL_TIME)
                            .data(mapToAgeAnalyticReportData(accumulatedAnalyticReport.getTerminalBirthYearDistribution()))
                            .build(),
                    AnalyticReport.builder()
                            .companyId(companyId)
                            .name(REPORT_NAME_AGE)
                            .timePeriod(TimePeriod.MONTH)
                            .data(mapToAgeAnalyticReportData(accumulatedAnalyticReport.getTerminalBirthYearDistributionForLastMonth()))
                            .build(),
                    AnalyticReport.builder()
                            .companyId(companyId)
                            .name(REPORT_NAME_AGE)
                            .timePeriod(TimePeriod.WEEK)
                            .data(mapToAgeAnalyticReportData(accumulatedAnalyticReport.getTerminalBirthYearDistributionForLastWeek()))
                            .build(),
                    AnalyticReport.builder()
                            .companyId(companyId)
                            .name(REPORT_NAME_CITY)
                            .timePeriod(TimePeriod.ALL_TIME)
                            .data(mapToCityAnalyticReportData(accumulatedAnalyticReport.getTerminalCityDistribution()))
                            .build(),
                    AnalyticReport.builder()
                            .companyId(companyId)
                            .name(REPORT_NAME_CITY)
                            .timePeriod(TimePeriod.MONTH)
                            .data(mapToCityAnalyticReportData(accumulatedAnalyticReport.getTerminalCityDistributionForLastMonth()))
                            .build(),
                    AnalyticReport.builder()
                            .companyId(companyId)
                            .name(REPORT_NAME_CITY)
                            .timePeriod(TimePeriod.WEEK)
                            .data(mapToCityAnalyticReportData(accumulatedAnalyticReport.getTerminalCityDistributionForLastWeek()))
                            .build(),
                    AnalyticReport.builder()
                            .companyId(companyId)
                            .name(REPORT_NAME_VENDOR)
                            .timePeriod(TimePeriod.ALL_TIME)
                            .data(mapToVendorAnalyticReportData(accumulatedAnalyticReport.getTerminalVendorDistribution()))
                            .build(),
                    AnalyticReport.builder()
                            .companyId(companyId)
                            .name(REPORT_NAME_VENDOR)
                            .timePeriod(TimePeriod.MONTH)
                            .data(mapToVendorAnalyticReportData(accumulatedAnalyticReport.getTerminalVendorDistributionForLastMonth()))
                            .build(),
                    AnalyticReport.builder()
                            .companyId(companyId)
                            .name(REPORT_NAME_VENDOR)
                            .timePeriod(TimePeriod.WEEK)
                            .data(mapToVendorAnalyticReportData(accumulatedAnalyticReport.getTerminalVendorDistributionForLastWeek()))
                            .build()
            ));
        });
    }

    private Year getTerminalBirthYear(Terminal terminal) {
        Object value = terminal.getProp(Terminal.PROP_KEY_BIRTH_DATE);
        if (value == null) {
            return null;
        }

        String string = String.valueOf(value);
        if (!string.matches("\\d+")) {
            return null;
        }

        return Year.of(Integer.valueOf(string));
    }

    private String getTerminalCity(Terminal terminal) {
        String city = Cities.formatCityName(terminal.getCity());
        if (city == null || city.trim().isEmpty()) {
            return null;
        }

        return city;
    }

    private String getTerminalVendor(Terminal terminal) {
        String vendor = terminal.getVendor();
        if (vendor == null || vendor.trim().isEmpty()) {
            return null;
        }

        return vendor.toLowerCase();
    }

    private List<AgePercentage> mapToAgeAnalyticReportData(Map<Year, Long> distribution) {
        long total = distribution.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        return distribution.entrySet().stream()
                .map((Map.Entry<Year, Long> entry) ->
                        AgePercentage.builder()
                                .id(String.valueOf(UUID.randomUUID()))
                                .ageRange(mapToAgeRange(entry.getKey()))
                                .count(entry.getValue())
                                .percentage(entry.getValue() / (double) total)
                                .build())
                .collect(Collectors.toList());
    }

    private String mapToAgeRange(Year year) {
        if (year == null) {
            return "UNKNOWN";
        }

        int age = Year.now().getValue() - year.getValue();
        if (0 <= age && age <= 17) {
            return "0-17";
        } else if (18 <= age && age <= 24) {
            return "18-24";
        } else if (25 <= age && age <= 34) {
            return "25-34";
        } else if (35 <= age && age <= 44) {
            return "35-44";
        } else if (45 <= age && age <= 54) {
            return "45-54";
        } else if (55 <= age) {
            return ">55";
        } else {
            return "UNKNOWN";
        }
    }

    private List<CityPercentage> mapToCityAnalyticReportData(Map<String, Long> distribution) {
        long total = distribution.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        return distribution.entrySet().stream()
                .map((Map.Entry<String, Long> entry) ->
                        CityPercentage.builder()
                                .id(String.valueOf(UUID.randomUUID()))
                                .city(entry.getKey() == null ? "UNKNOWN" : entry.getKey())
                                .count(entry.getValue())
                                .percentage(entry.getValue() / (double) total)
                                .build())
                .collect(Collectors.toList());
    }

    private List<VendorPercentage> mapToVendorAnalyticReportData(Map<String, Long> distribution) {
        long total = distribution.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        return distribution.entrySet().stream()
                .map((Map.Entry<String, Long> entry) ->
                        VendorPercentage.builder()
                                .id(String.valueOf(UUID.randomUUID()))
                                .vendor(entry.getKey() == null ? "UNKNOWN" : entry.getKey())
                                .count(entry.getValue())
                                .percentage(entry.getValue() / (double) total)
                                .build())
                .collect(Collectors.toList());
    }
}
