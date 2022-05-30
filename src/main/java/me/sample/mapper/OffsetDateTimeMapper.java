package me.sample.mapper;

import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Mapper
public interface OffsetDateTimeMapper {

    default LocalDateTime asLocalDateTime(OffsetDateTime offsetDateTime) {
        return Optional.ofNullable(offsetDateTime)
                .map(OffsetDateTime::toLocalDateTime)
                .orElse(null);
    }

    default OffsetDateTime asOffsetDateTime(LocalDateTime localDateTime) {
        return Optional.ofNullable(localDateTime)
                .map(ldt -> ldt.atOffset(ZoneOffset.UTC))
                .orElse(null);
    }
}
