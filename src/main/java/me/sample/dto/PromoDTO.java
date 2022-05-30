package me.sample.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.sample.domain.PromoState;
import me.sample.domain.Source;
import me.sample.web.rest.validation.ValidPromoPartnersAndStores;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


/**
 * Акция
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ValidPromoPartnersAndStores
public class PromoDTO {

    UUID id;

    LocalDateTime createdDate;

    LocalDateTime updatedDate;

    Source source;

    @NotNull
    PromoState state;

    Boolean hidden;

    LocalDateTime activatedAt;

    LocalDateTime deactivatedAt;

    String name;

    String description;

    String imageUrl;

    @Builder.Default
    List<PartnerDTO> partners = new LinkedList<>();

    @Builder.Default
    List<StoreDTO> stores = new LinkedList<>();
}
