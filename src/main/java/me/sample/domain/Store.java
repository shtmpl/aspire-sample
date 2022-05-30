package me.sample.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EntityResult;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Торговая точка
 */

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@DynamicInsert
@TypeDef(name = "pgsql_enum", typeClass = PostgresqlEnumType.class)
@NamedNativeQuery(
        name = "StoreWithDistanceToGeoposition",
        query = "WITH search AS ( " +
                "  SELECT max(radius) AS radius " +
                "  FROM campaign " +
                "  WHERE campaign.state = 'RUNNING' " +
                ") " +
                "SELECT store.*, " +
                "       earth_distance(ll_to_earth(store.lat, store.lon), ll_to_earth(:lat, :lon)) AS distance_to_geoposition " +
                "FROM store " +
                "WHERE earth_box(ll_to_earth(:lat, :lon), (SELECT coalesce(radius, 0) FROM search)) @> ll_to_earth(store.lat, store.lon)",
        resultSetMapping = "StoreWithDistanceToGeopositionMapping"
)
@SqlResultSetMapping(
        name = "StoreWithDistanceToGeopositionMapping",
        entities = {
                @EntityResult(entityClass = Store.class)
        },
        columns = {
                @ColumnResult(name = "distance_to_geoposition", type = Double.class)
        })
@Entity
public class Store extends AbstractIdentifiable<UUID> {

    @GenericGenerator(name = "assigned-uuid", strategy = "me.sample.model.AssignedUUIDGenerator")
    @GeneratedValue(generator = "assigned-uuid")
    @Id
    UUID id;

    @CreationTimestamp
    @Column(name = "cdat", updatable = false)
    LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "udat")
    LocalDateTime updatedDate;

    @Type(
            type = "pgsql_enum",
            parameters = {
                    @Parameter(name = "enumClass", value = "me.sample.model.Source")
            })
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "source_type")
    Source source;

    @Type(
            type = "pgsql_enum",
            parameters = {
                    @Parameter(name = "enumClass", value = "me.sample.model.StoreState")
            })
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "store_state_type")
    StoreState state;

    String name;

    String fiasCode;

    String kladrCode;

    String city;

    String address;

    Double lon;

    Double lat;

    @ManyToOne
    // FIXME: Move to presentation layer
    @JsonIgnoreProperties("shops")
    Partner partner;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "store_promo",
            joinColumns = @JoinColumn(name = "store_id"),
            inverseJoinColumns = @JoinColumn(name = "promo_id"))
    Set<Promo> promos = new LinkedHashSet<>();

    @Builder.Default
    @ManyToMany(mappedBy = "stores")
    Set<ScheduledGeoposDissemination> scheduledGeoposDisseminations = new LinkedHashSet<>();
}
