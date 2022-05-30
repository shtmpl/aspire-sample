package me.sample.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import me.sample.domain.Terminal;

import java.util.Optional;
import java.util.UUID;

public interface SecuredTerminalService {

    long countTerminals(Specification<Terminal> specification);

    Page<Terminal> findTerminals(Pageable pageable);

    Page<Terminal> findTerminals(Specification<Terminal> specification, Pageable pageable);

    Optional<Terminal> findTerminal(UUID id);
}
