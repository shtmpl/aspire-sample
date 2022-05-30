package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.BadResourceException;
import me.sample.domain.Property;
import me.sample.repository.PropertyRepository;
import me.sample.service.PropertyService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@CacheConfig(cacheNames = PropertyServiceImpl.PROPERTY_CACHE)
@Transactional
@Service
public class PropertyServiceImpl implements PropertyService {

    public static final String PROPERTY_CACHE = "properties";

    PropertyRepository propertyRepository;

    @Cacheable
    @Transactional(readOnly = true)
    @Override
    public Page<Property> findProperties(Pageable pageable) {
        return propertyRepository.findAll(pageable);
    }

    @Cacheable(unless = "#result == null")
    @Transactional(readOnly = true)
    @Override
    public Optional<Property> findProperty(UUID id) {
        log.info(".findProperty(id: {})", id);

        return propertyRepository.findById(id);
    }

    @Cacheable(unless = "#result == null")
    @Transactional(readOnly = true)
    @Override
    public Optional<Property> findPropertyByName(String name) {
        log.info(".findPropertyByName(name: {})", name);

        return propertyRepository.findByName(name);
    }

    @CacheEvict
    @Override
    public Property saveProperty(Property data) {
        log.debug(".saveProperty()");

        UUID id = data.getId();
        if (id != null && propertyRepository.existsById(id)) {
            throw new BadResourceException(String.format("Property already exists for id: %s", id));
        }

        return propertyRepository.save(data);
    }

    @CacheEvict
    @Override
    public Optional<Property> updateProperty(UUID id, Property data) {
        log.debug(".updateProperty(id: {})", id);

        Property found = propertyRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        Property.Type type = data.getType();
        if (type != null && type != found.getType()) {
            found.setType(type);
        }


        String value = data.getValue();
        if (value != null && !value.equals(found.getValue())) {
            found.setValue(value);
        }

        String description = data.getDescription();
        if (description != null && !description.equals(found.getDescription())) {
            found.setDescription(description);
        }

        return Optional.of(propertyRepository.save(found));
    }

    @CacheEvict
    @Override
    public Optional<UUID> deleteProperty(@NonNull UUID id) {
        log.debug(".deleteProperty(id: {})", id);

        Property found = propertyRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        propertyRepository.delete(found);

        return Optional.of(id);
    }
}
