package me.sample.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.time.Year;
import java.util.LinkedHashMap;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Getter
@Setter
public class AccumulatedAnalyticReport {

    @Builder.Default
    Map<Year, Long> terminalBirthYearDistribution = new LinkedHashMap<>();

    @Builder.Default
    Map<Year, Long> terminalBirthYearDistributionForLastMonth = new LinkedHashMap<>();

    @Builder.Default
    Map<Year, Long> terminalBirthYearDistributionForLastWeek = new LinkedHashMap<>();

    @Builder.Default
    Map<String, Long> terminalCityDistribution = new LinkedHashMap<>();

    @Builder.Default
    Map<String, Long> terminalCityDistributionForLastMonth = new LinkedHashMap<>();

    @Builder.Default
    Map<String, Long> terminalCityDistributionForLastWeek = new LinkedHashMap<>();

    @Builder.Default
    Map<String, Long> terminalVendorDistribution = new LinkedHashMap<>();

    @Builder.Default
    Map<String, Long> terminalVendorDistributionForLastMonth = new LinkedHashMap<>();

    @Builder.Default
    Map<String, Long> terminalVendorDistributionForLastWeek = new LinkedHashMap<>();

    public AccumulatedAnalyticReport addTerminalBirthYearToDistribution(Year year) {
        terminalBirthYearDistribution.merge(year, 1L, Long::sum);

        return this;
    }

    public AccumulatedAnalyticReport addTerminalBirthYearToLastMonthDistribution(Year year) {
        terminalBirthYearDistributionForLastMonth.merge(year, 1L, Long::sum);

        return this;
    }

    public AccumulatedAnalyticReport addTerminalBirthYearToLastWeekDistribution(Year year) {
        terminalBirthYearDistributionForLastWeek.merge(year, 1L, Long::sum);

        return this;
    }

    public AccumulatedAnalyticReport addTerminalCityToDistribution(String city) {
        terminalCityDistribution.merge(city, 1L, Long::sum);

        return this;
    }

    public AccumulatedAnalyticReport addTerminalCityToLastMonthDistribution(String city) {
        terminalCityDistributionForLastMonth.merge(city, 1L, Long::sum);

        return this;
    }

    public AccumulatedAnalyticReport addTerminalCityToLastWeekDistribution(String city) {
        terminalCityDistributionForLastWeek.merge(city, 1L, Long::sum);

        return this;
    }

    public AccumulatedAnalyticReport addTerminalVendorToDistribution(String vendor) {
        terminalVendorDistribution.merge(vendor, 1L, Long::sum);

        return this;
    }

    public AccumulatedAnalyticReport addTerminalVendorToLastMonthDistribution(String vendor) {
        terminalVendorDistributionForLastMonth.merge(vendor, 1L, Long::sum);

        return this;
    }

    public AccumulatedAnalyticReport addTerminalVendorToLastWeekDistribution(String vendor) {
        terminalVendorDistributionForLastWeek.merge(vendor, 1L, Long::sum);

        return this;
    }
}
