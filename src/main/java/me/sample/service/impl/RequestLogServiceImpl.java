package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.RequestLog;
import me.sample.domain.Terminal;
import me.sample.repository.RequestLogRepository;
import me.sample.service.RequestLogService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@Service
public class RequestLogServiceImpl implements RequestLogService {

    RequestLogRepository requestLogRepository;

    @NonFinal
    @Value("${request-log.enable}")
    Boolean logEnable;

    @Transactional(readOnly = true)
    @Override
    public Page<RequestLog> findRequestLogs(Specification<RequestLog> specification, Pageable pageable) {
        return requestLogRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<RequestLog> findRequestLog(UUID id) {
        return requestLogRepository.findById(id);
    }

    @Async
    @Override
    public void collectRequestLogAsync(@NonNull String request, Object data, Terminal terminal) {
        if (!logEnable) {
            return;
        }

        requestLogRepository.save(RequestLog.builder()
                .terminal(terminal)
                .request(request)
                .data(data)
                .build());
    }
}
