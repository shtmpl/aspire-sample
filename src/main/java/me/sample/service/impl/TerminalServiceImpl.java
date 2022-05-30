package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import me.sample.dto.TerminalDTO;
import me.sample.domain.GeoPositionInfo;
import me.sample.domain.TerminalPlatform;
import me.sample.gateway.dadata.DadataGateway;
import me.sample.domain.Application;
import me.sample.domain.NotFoundResourceException;
import me.sample.mapper.TerminalMapper;
import me.sample.domain.Terminal;
import me.sample.domain.TerminalSpecifications;
import me.sample.domain.TestTerminalMaxCountExceededException;
import me.sample.repository.GeoPositionInfoRepository;
import me.sample.repository.TerminalRepository;
import me.sample.service.ApplicationService;
import me.sample.service.TerminalService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@CacheConfig(cacheNames = TerminalServiceImpl.CACHE_NAME_TERMINAL)
@Transactional
@Service
public class TerminalServiceImpl implements TerminalService {

    public static final String CACHE_NAME_TERMINAL = "terminal";

    private static final int BATCH_SIZE_TERMINAL_CITY_UPDATE = 1000;


    @NonFinal
    TerminalService self;

    TerminalRepository terminalRepository;
    GeoPositionInfoRepository geoPositionInfoRepository;

    ApplicationService applicationService;
    TerminalMapper terminalMapper;
    DadataGateway dadataGateway;

    @NonFinal
    @Value("${terminal.test.max.count}")
    Long testTerminalsMaxCount;

    @NonFinal
    @Value("${terminal.city.update-after-hours}")
    Integer updateLocationAfterHours;

    @Autowired
    public void setSelf(@Lazy TerminalService self) {
        this.self = self;
    }

    @Transactional(readOnly = true)
    @Override
    public long countTerminals(Specification<Terminal> specification) {
        return terminalRepository.count(specification);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Terminal> findTerminals(Pageable pageable) {
        return terminalRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Terminal> findTerminals(Specification<Terminal> specification, Pageable pageable) {
        return terminalRepository.findAll(specification, pageable);
    }

    @Override
    public List<Terminal> findTerminals(Specification<Terminal> specification) {
        return terminalRepository.findAll(specification);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Terminal> findTerminal(UUID id) {
        return terminalRepository.findById(id);
    }

    @Cacheable(key = "{#hardwareId, #applicationApiKey}", unless = "#result == null")
    @Transactional(readOnly = true)
    @Override
    public Optional<Terminal> findTerminal(@NonNull String hardwareId, @NonNull String applicationApiKey) {
        log.debug(".findTerminal(hardwareId: {}, applicationApiKey: {})", hardwareId, applicationApiKey);

        return terminalRepository.findFirstByHardwareIdAndApplicationApiKeyOrderByUpdatedDateDesc(hardwareId, applicationApiKey);
    }

    @Override
    public Terminal saveOrUpdateTerminal(TerminalDTO dto) {
        String applicationApiKey = dto.getAppBundle();
        Application application = applicationService.findApplicationByApiKey(applicationApiKey)
                .orElseThrow(() -> new NotFoundResourceException("Application", applicationApiKey));

        return saveOrUpdateTerminal(terminalMapper.toEntity(dto).setApplication(application));
    }

    @Async
    @Override
    public CompletableFuture<Terminal> saveOrUpdateTerminalAsync(TerminalDTO dto) {
        return CompletableFuture.completedFuture(saveOrUpdateTerminal(dto));
    }

    private Terminal saveOrUpdateTerminal(Terminal data) {
        return self.findTerminal(data.getHardwareId(), data.getApplication().getApiKey())
                .map((Terminal found) -> self.updateTerminal(found, data))
                .orElseGet(() -> self.saveTerminal(data));
    }

    @CachePut(key = "{#data.hardwareId, #data.application.apiKey}")
    @Override
    public Terminal saveTerminal(Terminal data) {
        log.debug(".saveTerminal()");

        return terminalRepository.save(data);
    }

    @Override
    public Optional<Terminal> updateTerminal(UUID id, Terminal data) {
        return terminalRepository.findById(id)
                .map((Terminal found) -> self.updateTerminal(found, data));
    }

    @CachePut(key = "{#found.hardwareId, #found.application.apiKey}")
    @Override
    public Terminal updateTerminal(Terminal found, Terminal data) {
        log.debug(".updateTerminal(id: {})", found.getId());

        boolean dirty = false;

        Boolean test = data.getTest();
        if (test != null && !test.equals(found.getTest())) {
            if (Boolean.TRUE.equals(test)) { // Updating otherwise not test terminal to be the one
                Long testTerminalsCount = terminalRepository.countByTest(true);
                if (testTerminalsMaxCount <= testTerminalsCount) {
                    throw new TestTerminalMaxCountExceededException(String.format(
                            "Количество тестовых терминалов: %s превышает максимальное допустимое количество тестовых терминалов: %s",
                            testTerminalsCount,
                            testTerminalsMaxCount));
                }
            }

            log.debug(".test: {} -> {}", found.getTest(), test);
            found.setTest(test);

            dirty = true;
        }

        TerminalPlatform platform = data.getPlatform();
        if (platform != null && !platform.equals(found.getPlatform())) {
            log.debug(".platform: {} -> {}", found.getPlatform(), platform);
            found.setPlatform(platform);

            dirty = true;
        }

        String vendor = data.getVendor();
        if (vendor != null && !vendor.equals(found.getVendor())) {
            log.debug(".vendor: {} -> {}", found.getVendor(), vendor);
            found.setVendor(vendor);

            dirty = true;
        }

        String model = data.getModel();
        if (model != null && !model.equals(found.getModel())) {
            log.debug(".model: {} -> {}", found.getModel(), model);
            found.setModel(model);

            dirty = true;
        }

        String osVersion = data.getOsVersion();
        if (osVersion != null && !osVersion.equals(found.getOsVersion())) {
            log.debug(".osVersion: {} -> {}", found.getOsVersion(), osVersion);
            found.setOsVersion(osVersion);

            dirty = true;
        }

        String appVersion = data.getAppVersion();
        if (appVersion != null && !appVersion.equals(found.getAppVersion())) {
            log.debug(".appVersion: {} -> {}", found.getAppVersion(), appVersion);
            found.setAppVersion(appVersion);

            dirty = true;
        }

        String msisdn = data.getMsisdn();
        if (msisdn != null && !msisdn.equals(found.getMsisdn())) {
            log.debug(".msisdn: {} -> {}", found.getMsisdn(), msisdn);
            found.setMsisdn(msisdn);

            dirty = true;
        }

        String pushId = data.getPushId();
        if (pushId != null && !pushId.equals(found.getPushId())) {
            log.debug(".pushId: {} -> {}", found.getPushId(), pushId);
            found.setPushId(pushId);

            dirty = true;
        }

        String ip = data.getIp();
        if (ip != null && !ip.equals(found.getIp())) {
            log.debug(".ip: {} -> {}", found.getIp(), ip);
            found.setIp(ip);

            dirty = true;
        }

        Map<String, Object> props = data.getProps();
        if (props != null && !props.isEmpty()) {
            for (Map.Entry<String, Object> entry : props.entrySet()) {
                String key = entry.getKey();
                if (key == null || key.trim().isEmpty()) {
                    continue;
                }

                Object value = entry.getValue();
                if (value != null && !Objects.equals(value, found.getProp(key))) {
                    if (Terminal.PROP_KEY_BIRTH_DATE.equals(key)) {
                        if (String.valueOf(value).matches("\\d+")) {
                            value = Integer.valueOf(String.valueOf(value));
                        } else {
                            log.debug(".props.{}: {} -> {} // Ignored. Reason: Invalid format",
                                    Terminal.PROP_KEY_BIRTH_DATE,
                                    found.getProp(key),
                                    value);

                            continue;
                        }
                    }

                    log.debug(".props.{}: {} -> {}", key, found.getProp(key), value);
                    found.setProp(key, value);

                    dirty = true;
                }
            }
        }

        return dirty ? terminalRepository.save(found) : found;
    }

    @CachePut(key = "{#found.hardwareId, #found.application.apiKey}")
    @Override
    public Terminal updateTerminalCityByIp(Terminal found, String ip) {
        log.debug(".updateTerminalCityByIp(id: {}, ip: {})", found.getId(), ip);
        if (ip == null) {
            return found;
        }

        LocalDateTime now = LocalDateTime.now();

        if (found.getCity() != null && !found.getCity().trim().isEmpty() &&
                found.getLastLocationUpdate() != null &&
                Duration.between(found.getLastLocationUpdate(), now).toHours() < updateLocationAfterHours) {
            log.debug("Terminal city update ignored. Reason: lastLocationUpdate: {} happened less than {} hours ago from now: {}",
                    found.getLastLocationUpdate(),
                    updateLocationAfterHours,
                    now);

            return found;
        }

        String city = dadataGateway.findCityByIp(ip)
                .orElse(null);
        if (city != null && !city.equals(found.getCity())) {
            log.debug(".city: {} -> {}", found.getCity(), city);
            found.setCity(city);

            log.debug(".lastLocationUpdate: {} -> {}", found.getLastLocationUpdate(), now);
            found.setLastLocationUpdate(now);
        }

        return terminalRepository.save(found);
    }

    @Override
    public CompletableFuture<Terminal> updateTerminalCityByIpAsync(Terminal found, String ip) {
        return CompletableFuture.completedFuture(self.updateTerminalCityByIp(found, ip));
    }

    @CacheEvict(allEntries = true)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public long syncTerminalCities() {
        log.debug(".syncTerminalCities()");

        long result = 0;
        Page<Terminal> page;
        while ((page = terminalRepository.findAll(
                TerminalSpecifications.cityIsBlank()
                        .and(Specification.not(TerminalSpecifications.ipIsNull())),
                PageRequest.of(0, BATCH_SIZE_TERMINAL_CITY_UPDATE))).hasContent()) {
            log.info("Sync'ing city for {} terminals...", page.getNumberOfElements());
            result += page.getNumberOfElements();

            terminalRepository.saveAll(page.map(this::alterTerminalCity));
        }

        return result;
    }

    @CachePut(key = "{#found.hardwareId, #found.application.apiKey}")
    @Override
    public Terminal syncTerminalCity(Terminal found) {
        log.debug(".syncTerminalCity(id: {})", found.getId());

        return terminalRepository.save(alterTerminalCity(found));
    }

    private Terminal alterTerminalCity(Terminal found) {
        LocalDateTime now = LocalDateTime.now();

        String city = dadataGateway.findCityByIp(found.getIp())
                .orElseGet(() -> geoPositionInfoRepository.findFirstByTerminalIdOrderByCreatedDateDesc(found.getId())
                        .flatMap((GeoPositionInfo lastKnownGeoposition) ->
                                dadataGateway.findCityByCoordinates(lastKnownGeoposition.getLat(), lastKnownGeoposition.getLon()))
                        .orElse(null));
        if (city != null && !city.equals(found.getCity())) {
            log.debug(".city: {} -> {}", found.getCity(), city);
            found.setCity(city);

            log.debug(".lastLocationUpdate: {} -> {}", found.getLastLocationUpdate(), now);
            found.setLastLocationUpdate(now);
        }

        return found;
    }

    @CacheEvict(allEntries = true)
    @Override
    public Optional<UUID> deleteTerminal(UUID id) {
        log.debug("Request to delete Terminal : {}", id);
        Terminal found = terminalRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        terminalRepository.delete(found);

        return Optional.of(id);
    }

//    @Transactional(readOnly = true)
//    @Override
//    public UUID sendTestPush(UUID terminalId, UUID geoposDisseminationId) {
//        log.info("Send test push. terminalId: {}, geoposDisseminationId: {}", terminalId, geoposDisseminationId);
//
//        Terminal terminal = terminalRepository.getOne(terminalId);
//        return geoposDisseminationService.findDissemination(geoposDisseminationId)
//                .map(geoposDissemination -> {
//                    NotificationTemplate notificationTemplate = geoposDissemination.getNotificationTemplate();
//                    PushNotification pushNotification = PushNotification.builder()
//                            .geoposDisseminationId(geoposDissemination.getId().toString())
//                            .text(notificationTemplate.getText())
//                            .title(notificationTemplate.getSubject())
//                            .customDataName(notificationTemplate.getCustomPushPartName())
//                            .customDataValue(notificationTemplate.getCustomPushPartValue())
//                            .build();
//                    return notificationService.notifyUser(terminal, pushNotification);
//                })
//                .map(Notification::getId)
//                .orElseThrow(NullPointerException::new);
//    }
//
//    @Transactional(readOnly = true)
//    @Override
//    public TerminalImportReportDTO checkImport(@NonNull List<TerminalDTO> terminalImports) {
//        TerminalImportReportDTO report = TerminalImportReportDTO.builder()
//                .newCount(0)
//                .updateCount(0)
//                .build();
//        Set<String> allowedApiKeys = applicationService.findAllowToWriteApiKeys();
//
//        terminalImports.stream()
//                .peek(ti -> Optional.of(ti.getAppBundle())
//                        .filter(allowedApiKeys::contains)
//                        .orElseThrow(() -> new UnknownAppBundleKeyException("Unknown app bundle key: " + ti.getAppBundle())))
//                .map(ti -> Tuple.of(ti, findTerminal(ti.getHardwareId(), ti.getAppBundle()).orElse(null)))
//                .forEach(tuple -> Match(tuple._2).of(
//                        Case($(isNull()), __ -> report.newIncrease()),
//                        Case($(t -> !Objects.equals(tuple._1.getProps(), tuple._2.getProps())), __ -> report.updateIncrease()),
//                        Case($(), 0)));
//
//        return report;
//    }
//
//    @Override
//    public void saveImport(@NonNull List<TerminalDTO> terminalImports) {
//        Set<String> allowedApiKeys = applicationService.findAllowToWriteApiKeys();
//
//        terminalImports.stream()
//                .peek(ti -> Optional.of(ti.getAppBundle())
//                        .filter(allowedApiKeys::contains)
//                        .orElseThrow(() -> new UnknownAppBundleKeyException("Unknown app bundle key: " + ti.getAppBundle())))
//                .forEach(this::saveOrUpdateTerminal);
//    }
}
