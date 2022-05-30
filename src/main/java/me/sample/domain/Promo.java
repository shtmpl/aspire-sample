package me.sample.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Акция
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@TypeDef(name = "pgsql_enum", typeClass = PostgresqlEnumType.class)
@Table(name = "promo")
@Entity
public class Promo extends AbstractIdentifiable<UUID> {

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
                    @Parameter(name = "enumClass", value = "me.sample.model.PromoState")
            })
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "promo_state_type")
    PromoState state;

    Boolean hidden;

    LocalDateTime activatedAt;

    LocalDateTime deactivatedAt;

    String name;

    String description;

    String imageUrl;

    @Builder.Default
    @ManyToMany(mappedBy = "promos")
    Set<Partner> partners = new LinkedHashSet<>();

    @Builder.Default
    @ManyToMany(mappedBy = "promos")
    Set<Store> stores = new LinkedHashSet<>();
}
