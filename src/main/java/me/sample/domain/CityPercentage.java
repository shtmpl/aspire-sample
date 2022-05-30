package me.sample.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Getter
@Setter
@Entity
public class CityPercentage implements Serializable {

    public static CityPercentage fromMap(Map<String, Object> data) {
        return CityPercentage.builder()
                .id(data.get("id") == null ? null : String.valueOf(data.get("id")))
                .city(data.get("city") == null ? null : String.valueOf(data.get("city")))
                .count(data.get("count") == null ? null : Long.valueOf(String.valueOf(data.get("count"))))
                .percentage(data.get("percentage") == null ? null : Double.valueOf(String.valueOf(data.get("percentage"))))
                .build();
    }


    @JsonIgnore
    @Id
    String id;
    String city;
    Long count;
    Double percentage;

    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("city", city);
        result.put("count", count);
        result.put("percentage", percentage);

        return result;
    }
}
