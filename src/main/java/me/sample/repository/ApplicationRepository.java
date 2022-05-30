package me.sample.repository;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import me.sample.domain.Application;

import java.util.Optional;
import java.util.UUID;

@CacheConfig(cacheNames = ApplicationRepository.APPLICATION_CACHE)
public interface ApplicationRepository extends JpaRepository<Application, UUID>, JpaSpecificationExecutor<Application> {

    String APPLICATION_CACHE = "application";

    Optional<Application> findByApiKey(String apiKey);
}
