package me.sample.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import me.sample.domain.Property;

import java.util.Optional;
import java.util.UUID;

public interface PropertyService {

    Page<Property> findProperties(Pageable pageable);

    Optional<Property> findProperty(UUID id);

    Optional<Property> findPropertyByName(String name);

    Property saveProperty(Property data);

    Optional<Property> updateProperty(UUID id, Property data);

    Optional<UUID> deleteProperty(UUID id);
}
