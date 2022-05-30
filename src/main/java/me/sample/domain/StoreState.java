package me.sample.domain;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum StoreState {

    INACTIVE(Values.INACTIVE),

    ACTIVE(Values.ACTIVE);

    String value;

    public static class Values {

        public static final String INACTIVE = "INACTIVE";
        public static final String ACTIVE = "ACTIVE";
    }
}
