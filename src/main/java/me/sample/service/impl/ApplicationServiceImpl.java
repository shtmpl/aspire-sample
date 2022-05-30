package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.Application;
import me.sample.domain.BadResourceException;
import me.sample.repository.ApplicationRepository;
import me.sample.service.ApplicationService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@CacheConfig(cacheNames = ApplicationServiceImpl.APPLICATION_CACHE)
@Transactional
@Service
public class ApplicationServiceImpl implements ApplicationService {

    public static final String APPLICATION_CACHE = "application";

    ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<Application> findApplications(Specification<Application> specification, Pageable pageable) {
        return applicationRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Application> findApplication(UUID id) {
        return applicationRepository.findById(id);
    }

    @Cacheable(key = "'byKey_' + #apiKey")
    @Transactional(readOnly = true)
    @Override
    public Optional<Application> findApplicationByApiKey(String apiKey) {
        return applicationRepository.findByApiKey(apiKey);
    }

    @CacheEvict(allEntries = true)
    @Override
    public Application saveApplication(Application data) {
        UUID id = data.getId();
        if (id != null && applicationRepository.existsById(id)) {
            throw new BadResourceException(String.format("Application already exists for id: %s", id));
        }

        return applicationRepository.save(data);
    }

    @CacheEvict(allEntries = true)
    @Override
    public Optional<Application> updateApplication(UUID id, Application data) {
        return applicationRepository.findById(id)
                .map((Application found) -> updateApplication(found, data));
    }

    @CacheEvict(allEntries = true)
    @Override
    public Application updateApplication(Application found, Application data) {
        String name = data.getName();
        if (name != null && !name.equals(found.getName())) {
            found.setName(name);
        }

        return applicationRepository.save(found);
    }

    @CacheEvict(allEntries = true)
    @Override
    public Optional<UUID> deleteApplication(UUID id) {
        Application found = applicationRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        applicationRepository.delete(found);

        return Optional.of(id);
    }
}
