package me.sample.domain.geo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Getter
@Setter
public class Event {

    LocalDateTime at;

    Point point;

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Event that = (Event) other;

        return at.equals(that.at) && point.equals(that.point);
    }

    @Override
    public int hashCode() {
        int result = at.hashCode();
        result = 31 * result + point.hashCode();

        return result;
    }
}
