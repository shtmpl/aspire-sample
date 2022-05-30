package me.sample.mapper;

import me.sample.dto.RequestLogDTO;
import me.sample.domain.RequestLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RequestLogMapper {

    @Mappings({
            @Mapping(target = "terminalId", source = "terminal.id"),
            @Mapping(target = "date", source = "createdDate")
    })
    RequestLogDTO toDto(RequestLog entity);
}
