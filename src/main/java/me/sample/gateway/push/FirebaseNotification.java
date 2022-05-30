package me.sample.gateway.push;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class FirebaseNotification {
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static final MapLikeType MAP_TYPE = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);

    @Getter
    @JsonIgnore
    final UUID id;

    @Getter
    final String title;

    @Getter
    final String body;

    @Getter
    String data;

    @Getter
    @JsonIgnore
    final String customDataName;

    @Getter
    @JsonIgnore
    final String customDataValue;

    @SneakyThrows(JsonProcessingException.class)
    @Builder
    public FirebaseNotification(@NonNull UUID id,
                                String title,
                                String body,
                                String customDataName,
                                String customDataValue) {
        this.id = id;
        this.title = title;
        this.body = body;
        HashMap<String, String> data = new HashMap<>();
        data.put("id", id.toString());
        if (StringUtils.isNotEmpty(customDataName)) {
            this.customDataName = customDataName;
            this.customDataValue = customDataValue;
            data.put(customDataName, customDataValue);
        } else {
            this.customDataName = null;
            this.customDataValue = null;
        }
        this.data = OBJECT_MAPPER.writeValueAsString(data);
    }

    public Map<String, String> toMap() {
        return OBJECT_MAPPER.convertValue(this, MAP_TYPE);
    }
}
