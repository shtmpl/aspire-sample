package me.sample.domain;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Сущность инкапсулирует настройки рекламной компании {@link Campaign} типа PUSH (рассылка по расписанию).
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
@Table(name = "distribution")
@Entity
public class Distribution extends AbstractIdentifiable<UUID> {

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
    @Type(type = "jsonb")
    @Column(name = "filters", columnDefinition = "jsonb")
    List<PropFilter> filters = new LinkedList<>();

    LocalDate start;

    LocalDate end;

    String cron;

    @OneToOne(mappedBy = "distribution", cascade = CascadeType.ALL, orphanRemoval = true)
    Campaign campaign;
}
