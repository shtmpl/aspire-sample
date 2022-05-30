package me.sample.mapper;

import org.mapstruct.Mapper;
import me.sample.utils.UUIDHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Mapper
public interface UUIDMapper {

    default String asString(UUID uuid) {
        return Optional.ofNullable(uuid)
                .map(UUID::toString)
                .orElse(null);
    }

    default UUID asUUID(String s) {
        return Optional.ofNullable(s)
                .filter(StringUtils::isNotEmpty)
                .map(UUIDHelper::fromString)
                .orElse(null);
    }
}
