package me.sample.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.sample.domain.StoreState;
import me.sample.domain.Source;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class StoreDTO {

    UUID id;

    @NotNull
    PartnerDTO partner;

    Source source;

    @NotNull
    StoreState state;

    String name;

    String fiasCode;

    String kladrCode;

    String city;

    String address;

    Double lon;

    Double lat;

    Long radius;
}
