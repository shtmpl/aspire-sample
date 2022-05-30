package me.sample.domain.geo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import me.sample.domain.AbstractIdentifiable;
import me.sample.domain.GeoPositionInfo;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "geo_cluster_geoposition")
@Entity
public class ClusterGeoposition extends AbstractIdentifiable<ClusterGeopositionId> {

    @Builder.Default
    @EmbeddedId
    ClusterGeopositionId id = new ClusterGeopositionId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("clusterId")
    Cluster cluster;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("geopositionId")
    GeoPositionInfo geoposition;

    Boolean accepted;
}
