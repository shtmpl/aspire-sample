package me.sample.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
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
@DynamicInsert
@Table(name = "geo_pos_info")
@Entity
public class GeoPositionInfo extends AbstractIdentifiable<UUID> {

    @GenericGenerator(name = "assigned-uuid", strategy = "me.sample.model.AssignedUUIDGenerator")
    @GeneratedValue(generator = "assigned-uuid")
    @Id
    UUID id;

    @Column(name = "cdat", updatable = false)
    LocalDateTime createdDate;

    /**
     * Широта, N
     */
    Double lat;

    /**
     * Долгота, E
     */
    Double lon;

    @ManyToOne
    Terminal terminal;

    /**
     * Кластеризована ли геопозиция?
     * (Сервисное поле. Создано для ускорения выборки некластеризованных геопозиций)
     */
    Boolean clustered;

    @PrePersist
    public void prePersist() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }
}
