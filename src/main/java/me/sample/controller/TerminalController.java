package me.sample.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.NotificationService;
import me.sample.service.RequestLogService;
import me.sample.service.TerminalService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import me.sample.config.SwaggerConfiguration;
import me.sample.controller.argumentResolver.TerminalBind;
import me.sample.dto.GeoPositionInfoDTO;
import me.sample.dto.PushMessageDTO;
import me.sample.dto.PushTokenDTO;
import me.sample.dto.TerminalDTO;
import me.sample.domain.NotificationState;
import me.sample.domain.Terminal;
import me.sample.service.LocationConsumerService;

import javax.validation.Valid;
import java.util.Map;
import java.util.UUID;

/**
 * Важно! Запросы терминала могут дойти до бека в произвольном порядке
 */
@Api(tags = SwaggerConfiguration.TERMINAL)
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping(value = "/api/terminal")
@RestController
public class TerminalController {

    TerminalService terminalService;
    NotificationService notificationService;
    LocationConsumerService locationConsumerService;
    RequestLogService requestLogService;

    @NonFinal
    @Value("${terminal.city.sync-on-endpoint-invocation}")
    Boolean syncLocationOnEndpointInvocation;

    /**
     * Принимает текущую (условно) гео-локацию терминала
     */
    @TerminalApi
    @ApiOperation(nickname = "location", value = "${terminalController.location}")
    @PostMapping("/location")
    public void location(@TerminalBind @ApiIgnore TerminalDTO dto,
                         @Valid @RequestBody GeoPositionInfoDTO geoPosInfoDTO) {
        String applicationApiKey = dto.getAppBundle();
        String terminalHardwareId = dto.getHardwareId();
        String terminalIp = dto.getIp();
        Double geopositionLat = geoPosInfoDTO.getLat();
        Double geopositionLon = geoPosInfoDTO.getLon();
        log.info(".location(Terminal.hardwareId: {} .appBundle: {} .ip: {}, Geoposition.lat: {} .lon: {})",
                terminalHardwareId, applicationApiKey, terminalIp,
                geopositionLat, geopositionLon);

        Terminal terminal = terminalService.saveOrUpdateTerminal(dto);
        if (Boolean.TRUE.equals(syncLocationOnEndpointInvocation)) {
            terminalService.updateTerminalCityByIpAsync(terminal, terminalIp);
        }

        if (geopositionLat == null || geopositionLon == null) {
            log.warn("No coordinates provided. Application.apiKey: {}, Terminal.hardwareId: {}, Geoposition.lat: {} .lon: {}",
                    applicationApiKey,
                    terminalHardwareId,
                    geopositionLat, geopositionLon);
        } else {
            locationConsumerService.handleAsync(terminal, geoPosInfoDTO);
        }

        requestLogService.collectRequestLogAsync("location", geoPosInfoDTO, terminal);
    }

    /**
     * pushToken - идентификатор Firebase или Apns, для отправки push-сообщений.
     * Он обязателен для отправки уведомлений. Идентифицирует терминал.
     */
    @TerminalApi
    @ApiOperation(nickname = "pushToken", value = "${terminalController.pushToken}")
    @PostMapping("/pushToken")
    public void pushToken(@TerminalBind @ApiIgnore TerminalDTO dto,
                          @Valid @RequestBody PushTokenDTO pushToken) {
        log.debug(".pushToken(Terminal.hardwareId: {} .appBundle: {} .ip: {}, Token: {})",
                dto.getHardwareId(), dto.getAppBundle(), dto.getIp(),
                pushToken.getToken());

        Terminal terminal = terminalService.saveOrUpdateTerminal(dto.setPushId(pushToken.getToken()));
        if (Boolean.TRUE.equals(syncLocationOnEndpointInvocation)) {
            terminalService.updateTerminalCityByIpAsync(terminal, dto.getIp());
        }

        requestLogService.collectRequestLogAsync("pushToken", pushToken, terminal);
    }

    /**
     * Сообщает серверу, что данный пуш был открыт на устройстве
     */
    @TerminalApi
    @ApiOperation(nickname = "pushOpened", value = "${terminalController.pushOpened}")
    @PostMapping({"/pushRecieved", "/pushReceived", "/pushOpened"})
    public void pushOpened(@Valid @RequestBody PushMessageDTO pushMessageDTO) {
        log.debug(".pushOpened(Notification.id: {})", pushMessageDTO.getMessageId());

        UUID notificationId = UUID.fromString(pushMessageDTO.getMessageId());

        notificationService.updateNotificationStateAsync(notificationId, NotificationState.ACKNOWLEDGED_BY_CLIENT);
    }

    /**
     * Сообщает серверу, что данный пуш доставлен на устройство
     */
    @TerminalApi
    @ApiOperation(nickname = "pushDelivered", value = "${terminalController.pushDelivered}")
    @PostMapping("/pushDelivered")
    public void pushDelivered(@Valid @RequestBody PushMessageDTO pushMessageDTO) {
        log.debug(".pushDelivered(Notification.id: {})", pushMessageDTO.getMessageId());

        UUID notificationId = UUID.fromString(pushMessageDTO.getMessageId());

        notificationService.updateNotificationStateAsync(notificationId, NotificationState.RECEIVED_BY_CLIENT);
    }

    /**
     * Сообщает серверу известные/доступные параметры о пользователе
     */
    @TerminalApi
    @ApiOperation(nickname = "setProps", value = "${terminalController.setProps}")
    @PostMapping("/setProps")
    public void setProps(@TerminalBind @ApiIgnore TerminalDTO dto,
                         @Valid @RequestBody Map<String, Object> props) {
        log.debug(".setProps(Terminal.hardwareId: {} .appBundle: {} .ip: {}, Props: {})",
                dto.getHardwareId(), dto.getAppBundle(), dto.getIp(),
                props);

        Terminal terminal = terminalService.saveOrUpdateTerminal(dto.setProps(props));
        if (Boolean.TRUE.equals(syncLocationOnEndpointInvocation)) {
            terminalService.updateTerminalCityByIpAsync(terminal, dto.getIp());
        }

        requestLogService.collectRequestLogAsync("setProps", props, terminal);
    }
}
