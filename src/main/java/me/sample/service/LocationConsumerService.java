package me.sample.service;

import me.sample.dto.GeoPositionInfoDTO;
import me.sample.domain.Terminal;

public interface LocationConsumerService {

    void handleAsync(Terminal terminal, GeoPositionInfoDTO geoPosInfoDTO);
}
