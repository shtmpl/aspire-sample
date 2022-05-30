package me.sample.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import me.sample.domain.RequestLog;
import me.sample.domain.Terminal;

import java.util.Optional;
import java.util.UUID;

public interface RequestLogService {

    Page<RequestLog> findRequestLogs(Specification<RequestLog> specification, Pageable pageable);

    Optional<RequestLog> findRequestLog(UUID id);

    void collectRequestLogAsync(String request, Object data, Terminal terminal);
}
