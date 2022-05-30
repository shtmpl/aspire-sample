package me.sample.utils;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.regex.Pattern;

@UtilityClass
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParseUtils {
    private static final Pattern INTEGER_PATTERN = Pattern.compile("-?\\d+");

    public static Object parseToObject(String value) {
        if (NumberUtils.isParsable(value)) {
            if (INTEGER_PATTERN.matcher(value).matches()) {
                return Integer.valueOf(value);
            } else {
                return Float.valueOf(value);
            }
        } else {
            return value;
        }

    }
}
