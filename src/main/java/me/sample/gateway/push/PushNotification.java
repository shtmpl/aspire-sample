package me.sample.gateway.push;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Push-уведомление предназначенное для отправки в приложение
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PushNotification {

    public static final String TYPE = "pushActivity";


    @JsonIgnore
    private final String type;

    @Nullable
    @JsonProperty("group")
    private final String group;

    @JsonIgnore
    private final Map<String, Object> params;

    private final String text;

    /*@JsonProperty("activity_name")*/
    @JsonIgnore
    private final Activity activity;

    /*@JsonProperty("show_update_screen")*/
    @JsonIgnore
    private final Boolean showUpdateScreen;

    String campaignId;

    @JsonIgnore
    String title;

    @JsonIgnore
    LocalDateTime createTime;

    @JsonIgnore
    String customDataName;

    @JsonIgnore
    String customDataValue;

    @Builder
    public PushNotification(String campaignId,
                            String text,
                            String title,
                            String customDataName,
                            String customDataValue,
                            LocalDateTime createTime) {
        this.type = TYPE;
        this.group = null;
        this.params = Collections.emptyMap();

        this.text = text;

        this.activity = Activity.CAMPAIGN;
        this.showUpdateScreen = false;

        this.campaignId = campaignId;
        this.title = title;
        this.createTime = Optional.ofNullable(createTime).orElseGet(LocalDateTime::now);
        this.customDataName = customDataName;
        this.customDataValue = customDataValue;
    }

    public String getType() {
        return type;
    }

    public String getGroup() {
        return group;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public String getText() {
        return text;
    }

    public Activity getActivity() {
        return activity;
    }

    public Boolean getShowUpdateScreen() {
        return showUpdateScreen;
    }
}
