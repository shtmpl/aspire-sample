package me.sample.domain.geo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;

import java.time.Duration;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Getter
@Setter
public class EventDistance {

    Distance spatialDistance;

    Duration temporalDistance;

    public double getSpatialDistanceValueInMeters() {
        return spatialDistance.in(Metrics.KILOMETERS).getValue() * 1000;
    }

    public long getTemporalDistanceValueInMinutes() {
        return temporalDistance.toMinutes();
    }
}
