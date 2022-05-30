package me.sample.domain;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Parameter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Сущность партнер.
 * Отражает юридическое лицо, сотрудничающее с организацией осуществляющей push-рассылки.
 * Например, Банк и МВидео: банк будет представлен сущностью {@link Company},
 * а сеть магазинов - сущностью {@link Partner}.
 * Если сеть магазинов станет сотрудничать с еще одним банком в рамках системы,
 * то в таком случае эта сеть будет представлена в базе двумя сущностями {@link Partner}
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
@Entity
public class Partner extends AbstractIdentifiable<UUID> {

    @GenericGenerator(name = "assigned-uuid", strategy = "me.sample.model.AssignedUUIDGenerator")
    @GeneratedValue(generator = "assigned-uuid")
    @Id
    UUID id;

    @Column(name = "cdat", updatable = false)
    LocalDateTime createdDate;

    @Column(name = "udat")
    LocalDateTime updatedDate;

    @ManyToOne
    Company company;

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
                    @Parameter(name = "enumClass", value = "me.sample.model.PartnerState")
            })
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "partner_state_type")
    PartnerState state;

    String name;

    String descriptionFull;

    String descriptionShort;

    String siteUrl;

    /**
     * Доступна онлайн оплата?
     */
    Boolean sitePayment;

    /**
     * Доставка по всей территории РФ?
     */
    Boolean deliveryRussia;

    Boolean hasOnlineStore;

    Boolean hasOfflineStore;

    String pinIconUrl;

    String iconUrl;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "partner_promo",
            joinColumns = @JoinColumn(name = "partner_id"),
            inverseJoinColumns = @JoinColumn(name = "promo_id"))
    Set<Promo> promos = new LinkedHashSet<>();

    @Builder.Default
    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Store> stores = new LinkedHashSet<>();

    @Builder.Default
    @ManyToMany(mappedBy = "partners")
    Set<ScheduledGeoposDissemination> scheduledGeoposDisseminations = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (this.createdDate == null) {
            this.createdDate = now;
        }

        if (this.updatedDate == null) {
            this.updatedDate = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        LocalDateTime now = LocalDateTime.now();

        if (this.updatedDate == null) {
            this.updatedDate = now;
        }
    }
}
