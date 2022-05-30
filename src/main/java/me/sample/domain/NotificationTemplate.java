package me.sample.domain;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import me.sample.enumeration.Language;
import me.sample.enumeration.TemplateType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Шаблон push-уведомления
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "notification_template")
@Entity
public class NotificationTemplate extends AbstractIdentifiable<UUID> {

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

    @Builder.Default
    @Enumerated(EnumType.STRING)
    TemplateType type = TemplateType.GENERAL_PUSH;

    @Enumerated(EnumType.STRING)
    Language language;

    String name;

    String subject;

    String contentType;        // may be enum?

    @Column(name = "body")
    String text;

    String customPushPartName;

    String customPushPartValue;

    @ManyToOne
    Company company;

    @Builder.Default
    @OneToMany(mappedBy = "notificationTemplate")
    Set<Campaign> campaigns = new LinkedHashSet<>();
}
