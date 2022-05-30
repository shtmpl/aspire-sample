package me.sample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import me.sample.domain.Property;

import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {
    Optional<Property> findByName(String name);
}
