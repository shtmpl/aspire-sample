package me.sample.web.rest;

import io.swagger.annotations.Api;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.dto.NotificationDTO;
import me.sample.dto.NotificationSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import me.sample.config.SwaggerConfiguration;
import me.sample.domain.NotFoundResourceException;
import me.sample.mapper.NotificationMapper;
import me.sample.domain.Notification;
import me.sample.domain.NotificationSpecifications;
import me.sample.domain.Specifications;
import me.sample.domain.data.NotificationStateData;
import me.sample.service.NotificationDeliveryService;
import me.sample.service.SecuredNotificationService;
import me.sample.web.rest.request.NotificationSendRequest;
import me.sample.web.rest.response.NotificationSendResponse;
import me.sample.web.rest.util.PaginationUtil;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@Api(tags = SwaggerConfiguration.NOTIFICATION)
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
@RequestMapping("/api/notifications")
@RestController
public class NotificationResource {

    SecuredNotificationService securedNotificationService;
    NotificationDeliveryService notificationDeliveryService;

    NotificationMapper notificationMapper;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> index(Pageable pageable) {
        Page<Notification> notifications = securedNotificationService.findNotifications(pageable);
        Page<NotificationDTO> result = notifications.map(notificationMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/api/notifications"))
                .body(result.getContent());
    }

    @PostMapping("/search")
    public ResponseEntity<List<NotificationDTO>> search(@Valid @RequestBody NotificationSearchDTO request,
                                                        Pageable pageable) {
        Specification<Notification> specification = makeSpecification(request);
        Page<Notification> notifications = securedNotificationService.findNotifications(specification, pageable);
        Page<NotificationDTO> result = notifications.map(notificationMapper::toDto);

        return ResponseEntity.ok()
                .headers(PaginationUtil.generatePaginationHttpHeaders(result, "/api/notifications/search"))
                .body(result.getContent());
    }

    private Specification<Notification> makeSpecification(NotificationSearchDTO request) {
        Specification<Notification> result = Specifications.any();

        List<UUID> terminalIds = request.getTerminalIds();
        if (terminalIds != null && !terminalIds.isEmpty()) {
            result = result.and(NotificationSpecifications.terminalIdIn(terminalIds));
        }

        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> show(@PathVariable UUID id) {
        Notification notification = securedNotificationService.findNotification(id)
                .orElseThrow(() -> new NotFoundResourceException("Notification", id));

        return ResponseEntity.ok(notificationMapper.toDto(notification));
    }


    @PostMapping("/send")
    public ResponseEntity<NotificationSendResponse> send(@Valid @RequestBody NotificationSendRequest request) {
        NotificationStateData result = notificationDeliveryService.sendNotificationData(notificationMapper.toData(request));

        return ResponseEntity.ok(notificationMapper.toResponse(result));
    }
}
