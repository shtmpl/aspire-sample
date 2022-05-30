package me.sample.domain;

import org.apache.commons.lang.WordUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Cities {

    private static final Pattern CITY_NAME_QUALIFIED = Pattern.compile("г.\\s*(?<name>.*)");

    public static String formatCityName(String city) {
        if (city == null || city.trim().isEmpty()) {
            return "";
        }

        String result = city;
        Matcher matcher = CITY_NAME_QUALIFIED.matcher(city);
        if (matcher.matches()) {
            result = matcher.group("name");

            if (result == null || result.trim().isEmpty()) {
                return "";
            }
        }

        return WordUtils.capitalizeFully(result.trim().replaceAll("\\s+", " "), new char[]{' ', '-'});
    }
}
