package me.sample.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.sample.domain.DisseminationSchedule;
import me.sample.domain.PropFilter;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class DistributionDTO {

    UUID id;

    List<PropFilter> filters;

    @NotNull
    LocalDate start;

    @NotNull
    LocalDate end;

    @NotNull
    DisseminationSchedule schedule;
}
