package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.GeoPositionInfoService;
import me.sample.service.LocationConsumerService;
import me.sample.service.ScheduledGeoposDisseminationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.dto.GeoPositionInfoDTO;
import me.sample.mapper.GeoPositionInfoMapper;
import me.sample.domain.Terminal;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class LocationConsumerServiceImpl implements LocationConsumerService {

    GeoPositionInfoService geoPositionInfoService;
    GeoPositionInfoMapper geoPositionInfoMapper;
    ScheduledGeoposDisseminationService scheduledGeoposDisseminationService;

    @Async
    @Override
    public void handleAsync(Terminal terminal, GeoPositionInfoDTO geoPosInfoDTO) {
        geoPosInfoDTO.setTerminalId(terminal.getId().toString());
        geoPositionInfoService.saveGeopositionAsync(geoPositionInfoMapper.toEntity(geoPosInfoDTO));

        if (terminal.getPushId() == null || terminal.getPushId().trim().isEmpty()) {
            log.debug("No dissemination triggered for terminal id: {}. Reason: No pushId is provided for terminal",
                    terminal.getId());

            return;
        }

        scheduledGeoposDisseminationService.executeDisseminationForTerminal(
                terminal,
                geoPosInfoDTO.getLat(),
                geoPosInfoDTO.getLon());
    }
}
