package me.sample.domain.geo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Embeddable
public class ClusterGeopositionId implements Serializable {

    UUID clusterId;

    UUID geopositionId;

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        ClusterGeopositionId that = (ClusterGeopositionId) object;

        return Objects.equals(clusterId, that.clusterId) &&
                Objects.equals(geopositionId, that.geopositionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clusterId, geopositionId);
    }
}
