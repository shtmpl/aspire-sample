package me.sample.domain;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
@TypeDef(
        name = "pgsql_enum",
        typeClass = PostgresqlEnumType.class
)
@Table(name = "campaign")
@Entity
public class Campaign extends AbstractIdentifiable<UUID> {

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

    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum", parameters = {
            @Parameter(name = "enumClass", value = "me.sample.model.CampaignState")
    })
    @Column(columnDefinition = "campaign_state_type")
    CampaignState state;

    /**
     * Использует ли кампания список клиентов?
     */
    Boolean clientBased;

    @Transient
    Long clientCount;

    String name;

    String description;

    /**
     * Радиус захвата (в метрах)
     */
    Long radius;

    /**
     * Ограничение на кол-во уведомлений, успешно отправленных в рамках данной рассылки
     */
    Long notificationLimit;

    /**
     * Ограничение на кол-во уведомлений, успешно отправленных в рамках данной рассылки, для одного терминала
     */
    Long notificationLimitPerTerminal;

    /**
     * Общее кол-во уведомлений, созданных в рамках кампании.
     */
    Integer created;

    /**
     * Общее кол-во уведомлений, успешно отправленных на устройство в рамках кампании.
     */
    Integer sent;

    /**
     * Общее кол-во уведомлений, доставленных на устройство в рамках кампании.
     */
    Integer delivered;

    /**
     * Общее кол-во уведомлений, открытых на устройстве в рамках кампании.
     */
    Integer opened;

    /**
     * Общее кол-во уведомлений, неуспешно отправленных на устройство в рамках кампании.
     */
    Integer fail;

    @ManyToOne
    Company company;

    @ManyToOne
    NotificationTemplate notificationTemplate;

    @OneToOne
    Distribution distribution;

    @OneToOne
    ScheduledGeoposDissemination scheduledGeoposDissemination;

    @Builder.Default
    @OneToMany(mappedBy = "campaign", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Notification> notifications = new LinkedHashSet<>();
}
