package me.sample.service;

import me.sample.domain.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.UUID;

public interface ApplicationService {

    Page<Application> findApplications(Specification<Application> specification, Pageable pageable);

    Optional<Application> findApplication(UUID id);

    Optional<Application> findApplicationByApiKey(String apiKey);

    Application saveApplication(Application data);

    Optional<Application> updateApplication(UUID id, Application data);

    Application updateApplication(Application found, Application data);

    Optional<UUID> deleteApplication(UUID id);
}
