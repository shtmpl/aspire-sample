package me.sample.domain;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public enum TerminalPlatform {
    ANDROID("google-registrationId"),
    IOS("apple-deviceToken"),
    WP(null),
    WEB(null),
    SMS(null),
    TELEGRAM(null);

    final String pushIdParam;
    static final String APP_TYPE_APP = "APP";
    static final String APP_TYPE_WEB = "WEB";


    public static TerminalPlatform of(@NonNull String platformName) {
        return TerminalPlatform.valueOf(platformName.toUpperCase());
    }

    public String getPushIdParam() {
        if (pushIdParam == null) {
            throw new IllegalStateException("NotificationId parameter name unknown for platform " + name());
        }
        return pushIdParam;
    }

    public String getAppType() {
        switch (this) {
            case ANDROID:
                return APP_TYPE_APP;
            case IOS:
                return APP_TYPE_APP;
            case WEB:
                return APP_TYPE_WEB;
            default:
                return null;
        }
    }

}
