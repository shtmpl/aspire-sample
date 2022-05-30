package me.sample.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Рассылка, основанная на исторических данных о геопозиции утройства
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
@Table(name = "scheduled_geopos_dissemination")
@Entity
public class ScheduledGeoposDissemination extends AbstractIdentifiable<UUID> {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    UUID id;

    @CreationTimestamp
    @Column(name = "cdat", updatable = false)
    LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "udat")
    LocalDateTime updatedDate;

    @OneToOne(mappedBy = "scheduledGeoposDissemination", cascade = CascadeType.ALL, orphanRemoval = true)
    Campaign campaign;

    Boolean sendOnStand;

    Boolean sendOnWalk;

    Boolean sendOnRide;

    Integer priority;

    @Column(name = "start_date")
    LocalDateTime start;

    @Column(name = "end_date")
    LocalDateTime end;

    String cron;

    @Singular
    @Type(type = "jsonb")
    @Column(name = "filters", columnDefinition = "jsonb")
    List<PropFilter> filters = new LinkedList<>();

    /**
     * Партнеры, чьи торговые точки участвуют в рекламной кампании
     */
    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "scheduled_geopos_dissemination_partner",
            joinColumns = @JoinColumn(name = "scheduled_geopos_dissemination_id"),
            inverseJoinColumns = @JoinColumn(name = "partner_id"))
    Set<Partner> partners = new LinkedHashSet<>();

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "scheduled_geopos_dissemination_store",
            joinColumns = @JoinColumn(name = "scheduled_geopos_dissemination_id"),
            inverseJoinColumns = @JoinColumn(name = "store_id"))
    Set<Store> stores = new LinkedHashSet<>();
}
