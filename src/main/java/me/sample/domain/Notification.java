package me.sample.domain;

import lombok.*;
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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.EnumSet;
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
@Table(name = "notification")
@Entity
public class Notification extends AbstractIdentifiable<UUID> {

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
            @Parameter(name = "enumClass", value = "me.sample.model.NotificationState")
    })
    @Column(columnDefinition = "notification_state_type")
    NotificationState state;

    @Column(name = "body")
    String text;

    @ManyToOne
    Terminal terminal;

    @ManyToOne
    Campaign campaign;

    public boolean wasSent() {
        return EnumSet.of(
                NotificationState.ACCEPTED_BY_SERVER,
                NotificationState.RECEIVED_BY_CLIENT,
                NotificationState.ACKNOWLEDGED_BY_CLIENT)
                .contains(state);
    }
}
