package me.sample.service;

import me.sample.domain.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface SecuredApplicationService {

    Page<Application> findApplications(String search, Pageable pageable);

    Optional<Application> findApplication(UUID id);

    Application saveApplication(Application data);

    Optional<Application> updateApplication(UUID id, Application data);

    Optional<UUID> deleteApplication(UUID id);
}
