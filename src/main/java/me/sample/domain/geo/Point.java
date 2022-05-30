package me.sample.domain.geo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Getter
@Setter
public class Point {

    Double lat;

    Double lon;

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Point that = (Point) other;

        return lat.equals(that.lat) && lon.equals(that.lon);
    }

    @Override
    public int hashCode() {
        int result = lat.hashCode();
        result = 31 * result + lon.hashCode();

        return result;
    }
}
