package me.sample.domain.geo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import me.sample.domain.Terminal;
import org.hibernate.annotations.GenericGenerator;
import me.sample.domain.AbstractIdentifiable;
import me.sample.domain.GeoPositionInfo;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Getter
@Setter
@Table(name = "geo_cluster")
@Entity
public class Cluster extends AbstractIdentifiable<UUID> {

    @GenericGenerator(name = "assigned-uuid", strategy = "me.sample.model.AssignedUUIDGenerator")
    @GeneratedValue(generator = "assigned-uuid")
    @Id
    UUID id;

    Double lat;

    Double lon;

    Integer visitCount;

    LocalDateTime lastVisitedAt;

    @ManyToOne
    Terminal terminal;

    @Builder.Default
    @OneToMany(mappedBy = "cluster", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<ClusterGeoposition> geopositions = new LinkedHashSet<>();

    public Set<GeoPositionInfo> getAcceptedGeopositions() {
        return geopositions.stream()
                .filter((ClusterGeoposition clusterGeoposition) -> Boolean.TRUE.equals(clusterGeoposition.getAccepted()))
                .map(ClusterGeoposition::getGeoposition)
                .collect(Collectors.toSet());
    }

    public Set<GeoPositionInfo> getRejectedGeopositions() {
        return geopositions.stream()
                .filter((ClusterGeoposition clusterGeoposition) -> Boolean.FALSE.equals(clusterGeoposition.getAccepted()))
                .map(ClusterGeoposition::getGeoposition)
                .collect(Collectors.toSet());
    }
}
