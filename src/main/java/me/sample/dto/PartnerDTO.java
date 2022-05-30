package me.sample.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.sample.domain.PartnerState;
import me.sample.domain.Source;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PartnerDTO {

    UUID id;

    Source source;

    @NotNull
    PartnerState state;

    String name;

    String descriptionFull;

    String descriptionShort;

    String siteUrl;

    Boolean sitePayment;

    Boolean deliveryRussia;

    Boolean hasOnlineStore;

    Boolean hasOfflineStore;

    String pinIconUrl;

    String iconUrl;

    Integer storeCount;

    @NotNull
    CompanyDTO company;
}
