package me.sample.mapper;

import me.sample.dto.GeoPositionInfoDTO;
import me.sample.domain.GeoPositionInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(uses = {UUIDMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GeoPositionInfoMapper {

    @Mapping(target = "terminal.id", source = "terminalId")
    GeoPositionInfo toEntity(GeoPositionInfoDTO dto);

    @Mapping(target = "pushToken", source = "terminal.pushId")
    @Mapping(target = "terminalId", source = "terminal.id")
    @Mapping(target = "model", source = "terminal.model")
    @Mapping(target = "vendor", source = "terminal.vendor")
    @Mapping(target = "props", source = "terminal.props")
    @Mapping(target = "terminalCreatedDate", source = "terminal.createdDate")
    @Mapping(target = "terminalUpdatedDate", source = "terminal.updatedDate")
    GeoPositionInfoDTO toDto(GeoPositionInfo entity);
}
