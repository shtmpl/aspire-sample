package me.sample.service.properties;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.sample.service.PropertyService;
import org.springframework.stereotype.Service;
import me.sample.domain.Property;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Service
public class PushPropertiesProvider {

    PropertyService propertyService;

    private static final String MAX_PUSH_COUNT_MINUTE = "push.constraint.count.minute";
    private static final String MAX_PUSH_COUNT_HOUR = "push.constraint.count.hour";
    private static final String MAX_PUSH_COUNT_DAY = "push.constraint.count.day";

    public Integer getMaxPushOnMinute() {
        return propertyService.findPropertyByName(MAX_PUSH_COUNT_MINUTE)
                .map(Property::getIntegerValue)
                .orElse(null);
    }

    public Integer getMaxPushOnHour() {
        return propertyService.findPropertyByName(MAX_PUSH_COUNT_HOUR)
                .map(Property::getIntegerValue)
                .orElse(null);
    }

    public Integer getMaxPushOnDay() {
        return propertyService.findPropertyByName(MAX_PUSH_COUNT_DAY)
                .map(Property::getIntegerValue)
                .orElse(null);
    }
}
