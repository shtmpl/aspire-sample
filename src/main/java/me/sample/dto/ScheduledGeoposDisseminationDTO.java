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
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ScheduledGeoposDisseminationDTO {

    UUID id;

    Boolean sendOnStand;

    Boolean sendOnWalk;

    Boolean sendOnRide;

    @NotNull
    LocalDateTime start;

    @NotNull
    LocalDateTime end;

    @NotNull
    DisseminationSchedule schedule;

    Integer priority;

    List<PropFilter> filters;

    List<PartnerDTO> partners = new LinkedList<>();

    List<StoreDTO> stores = new LinkedList<>();
}
