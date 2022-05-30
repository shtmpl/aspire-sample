package me.sample.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@TypeDef(
        name = "pgsql_enum",
        typeClass = PostgresqlEnumType.class
)
@Table(name = "notification_state_log")
@Entity
public class NotificationStateLog extends AbstractIdentifiable<UUID> {

    @GenericGenerator(name = "assigned-uuid", strategy = "me.sample.model.AssignedUUIDGenerator")
    @GeneratedValue(generator = "assigned-uuid")
    @Id
    UUID id;

    LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum", parameters = {
            @Parameter(name = "enumClass", value = "me.sample.model.NotificationState")
    })
    @Column(columnDefinition = "notification_state_type")
    NotificationState state;

    /**
     * Причина неудачи отправки уведомления
     */
    String stateReason;

    @ManyToOne(fetch = FetchType.LAZY)
    Notification notification;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (this.createdAt == null) {
            this.createdAt = now;
        }
    }
}
