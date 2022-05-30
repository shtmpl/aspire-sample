package me.sample.mapper;

import me.sample.dto.TerminalDTO;
import me.sample.domain.Terminal;
import org.mapstruct.*;

import java.util.HashMap;

@Mapper(uses = UUIDMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = HashMap.class)
public interface TerminalMapper {

    @Mapping(target = "appBundle", source = "application.apiKey")
    @Mapping(target = "application", qualifiedBy = NoNestedEntity.class)
    TerminalDTO toDto(Terminal entity);

    @Mapping(target = "application", qualifiedBy = OnlyId.class)
    Terminal toEntity(TerminalDTO dto);
}
