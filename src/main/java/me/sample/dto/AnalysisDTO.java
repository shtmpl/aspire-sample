package me.sample.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import me.sample.domain.PropFilter;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Getter
@Setter
public class AnalysisDTO {

    @NotNull
    Long radius;

    @Singular
    List<PropFilter> clusterVisitCountFilters;

    @Singular
    List<PropFilter> terminalFilters;

    @Singular
    List<UUID> storePartnerIds;

    @Singular
    List<String> storeCities;
}
