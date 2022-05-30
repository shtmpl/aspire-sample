package me.sample.gateway.bank;

import me.sample.domain.Category;
import me.sample.domain.Partner;
import me.sample.domain.PartnerState;
import me.sample.domain.Promo;
import me.sample.domain.PromoState;
import me.sample.domain.Source;
import me.sample.domain.Store;
import me.sample.domain.StoreState;
import me.sample.service.BankSynchronizationService;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import me.sample.gateway.bank.response.ResponseCategory;
import me.sample.gateway.bank.response.ResponseImage;
import me.sample.gateway.bank.response.ResponseImageType;
import me.sample.gateway.bank.response.ResponsePartner;
import me.sample.gateway.bank.response.ResponsePromo;
import me.sample.gateway.bank.response.ResponseShop;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.WARN)
public interface BankApiMapper {

    default UUID mapToUUID(String value) {
        return UUID.fromString(value);
    }


    // Category

    @Mappings({
            @Mapping(target = "id", source = "response.id"),
            @Mapping(target = "createdDate", ignore = true),
            @Mapping(target = "updatedDate", ignore = true),
            @Mapping(target = "company.id", constant = BankSynchronizationService.COMPANY_ID_BANK),
            @Mapping(target = "source", constant = Source.Values.IMPORT_BANK),
            @Mapping(target = "name", source = "response.title"),
            @Mapping(target = "description", source = "response.description")
    })
    Category mapToCategory(ResponseCategory response);


    // Partner

    @Mappings({
            @Mapping(target = "id", source = "response.id"),
            @Mapping(target = "createdDate", source = "response.creationDate"),
            @Mapping(target = "updatedDate", source = "response.lastUpdateDate"), // Для новосозданной импортируемой сущности дата обновления null. См. this.setPartnerUpdatedDate()
            @Mapping(target = "company.id", constant = BankSynchronizationService.COMPANY_ID_BANK),
            @Mapping(target = "source", constant = Source.Values.IMPORT_BANK),
            @Mapping(target = "state", ignore = true), // See this.setPartnerState()
            @Mapping(target = "name", source = "response.title"),
            @Mapping(target = "siteUrl", ignore = true),
            @Mapping(target = "pinIconUrl", ignore = true),
            @Mapping(target = "iconUrl", ignore = true), // See this.setPartnerIconUrl()
            @Mapping(target = "stores", ignore = true),
            @Mapping(target = "promos", ignore = true)
    })
    Partner mapToPartner(ResponsePartner response);

    @AfterMapping
    default void setPartnerUpdatedDate(@MappingTarget Partner result, ResponsePartner response) {
        if (result.getUpdatedDate() == null) {
            result.setUpdatedDate(result.getCreatedDate());
        }
    }

    @AfterMapping
    default void setPartnerState(@MappingTarget Partner result, ResponsePartner response) {
        result.setState(Boolean.TRUE.equals(response.getIsActive()) ?
                PartnerState.ACTIVE :
                PartnerState.INACTIVE);
    }

    @AfterMapping
    default void setPartnerIconUrl(@MappingTarget Partner result, ResponsePartner response) {
        List<ResponseImage> images = response.getImages();
        if (images == null) {
            return;
        }

        images.stream()
                .filter((ResponseImage image) -> ResponseImageType.LOGO == image.getType())
                .findFirst()
                .map(ResponseImage::getUrl)
                .ifPresent(result::setIconUrl);
    }

    @AfterMapping
    default void setPartnerStores(@MappingTarget Partner result, ResponsePartner response) {
        List<UUID> storeIds = response.getShopsId();
        if (storeIds == null) {
            return;
        }

        result.setStores(storeIds.stream()
                .map((UUID id) -> Store.builder().id(id).build())
                .collect(Collectors.toSet()));
    }

    @AfterMapping
    default void setPartnerPromos(@MappingTarget Partner result, ResponsePartner response) {
        List<UUID> promoIds = response.getPromos();
        if (promoIds == null) {
            return;
        }

        result.setPromos(promoIds.stream()
                .map((UUID id) -> Promo.builder().id(id).build())
                .collect(Collectors.toSet()));
    }


    // Store

    @Mappings({
            @Mapping(target = "id", source = "response.id"),
            @Mapping(target = "createdDate", ignore = true),
            @Mapping(target = "updatedDate", ignore = true),
            @Mapping(target = "partner.id", source = "response.partnerId"),
            @Mapping(target = "source", constant = Source.Values.IMPORT_BANK),
            @Mapping(target = "state", constant = StoreState.Values.ACTIVE),
            @Mapping(target = "name", ignore = true), // See this.setStoreName()
            @Mapping(target = "fiasCode", source = "response.fiasCode"),
            @Mapping(target = "kladrCode", source = "response.kladrCode"),
            @Mapping(target = "lat", source = "response.location.coordinates.latitude"),
            @Mapping(target = "lon", source = "response.location.coordinates.longitude"),
            @Mapping(target = "city", source = "response.location.city"),
            @Mapping(target = "address", source = "response.location.address"),
            @Mapping(target = "promos", ignore = true)
    })
    Store mapToStore(ResponseShop response);

    @AfterMapping
    default void setStoreName(@MappingTarget Store result, ResponseShop response) {
        result.setName(String.join(", ", response.getLocation().getCity(), response.getLocation().getAddress()));
    }

    @AfterMapping
    default void setStorePromos(@MappingTarget Store result, ResponseShop response) {
        List<UUID> promoIds = response.getPromos();
        if (promoIds == null) {
            return;
        }

        result.setPromos(promoIds.stream()
                .map((UUID id) -> Promo.builder().id(id).build())
                .collect(Collectors.toSet()));
    }


    // Promo

    @Mappings({
            @Mapping(target = "id", source = "response.id"),
            @Mapping(target = "createdDate", ignore = true),
            @Mapping(target = "updatedDate", ignore = true),
            @Mapping(target = "source", constant = Source.Values.IMPORT_BANK),
            @Mapping(target = "state", constant = PromoState.Values.ACTIVE),
            @Mapping(target = "hidden", source = "response.isHide"),
            @Mapping(target = "activatedAt", source = "response.dateActivation", qualifiedBy = LocalDateTimeAtStartOfDay.class),
            @Mapping(target = "deactivatedAt", source = "response.dateDeactivation", qualifiedBy = LocalDateTimeAtEndOfDay.class),
            @Mapping(target = "name", source = "response.title"),
            @Mapping(target = "description", source = "response.description"),
            @Mapping(target = "imageUrl", ignore = true), // See this.setPromoImageUrl(),
            @Mapping(target = "partners", ignore = true),
            @Mapping(target = "stores", ignore = true)
    })
    Promo mapToPromo(ResponsePromo response);

    @AfterMapping
    default void setPromoImageUrl(@MappingTarget Promo result, ResponsePromo response) {
        List<ResponseImage> images = response.getImages();
        if (images == null || images.isEmpty()) {
            return;
        }

        images.stream()
                .filter((ResponseImage image) -> ResponseImageType.DEFAULT_PROMO == image.getType())
                .findFirst()
                .map(ResponseImage::getUrl)
                .ifPresent(result::setImageUrl);
    }

    @LocalDateTimeAtStartOfDay
    default LocalDateTime mapToPromoActivatedAt(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }

        return localDate.atTime(LocalTime.MIN);
    }

    @LocalDateTimeAtEndOfDay
    default LocalDateTime mapToPromoDeactivatedAt(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }

        return localDate.atTime(LocalTime.MAX);
    }
}
