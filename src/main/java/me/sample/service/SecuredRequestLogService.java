package me.sample.service;

import me.sample.domain.RequestLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.UUID;

public interface SecuredRequestLogService {

    Page<RequestLog> findRequestLogs(Specification<RequestLog> specification, Pageable pageable);

    Optional<RequestLog> findRequestLog(UUID id);
}
