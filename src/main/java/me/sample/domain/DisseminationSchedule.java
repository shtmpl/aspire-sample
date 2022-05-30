package me.sample.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.EnumSet;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public enum DisseminationSchedule {

    EVERY_MINUTE("0 * * ? * * *"), // For testing purposes
    EVERY_5_MINUTES("0 */5 * * * ?"),
    EVERY_DAY_AT_START_OF_DAY("0 0 0 * * ? *"),
    EVERY_SUNDAY_AT_MIDDLE_OF_DAY("0 0 12 ? * SUN *"),
    EVERY_WORKDAY_AT_MIDDLE_OF_DAY("0 0 12 ? * MON,TUE,WED,THU,FRI *");

    String cronExpression;

    public static DisseminationSchedule fromCronExpression(String cronExpression) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            return null;
        }

        return EnumSet.allOf(DisseminationSchedule.class)
                .stream()
                .filter((DisseminationSchedule it) -> it.cronExpression.equals(cronExpression))
                .findFirst()
                .orElse(null);
    }
}
