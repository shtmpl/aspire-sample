package me.sample.domain;


import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@DynamicInsert
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "code")
public class Code {

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

    @Enumerated(EnumType.STRING)
    CodeType type;
    @Enumerated(EnumType.STRING)
    CodeAccess access;
    @Column(name = "used")
    Boolean isUsed;
    String value;

    public enum CodeType {
        PROMO, QR, BAR
    }

    public enum CodeAccess {
        PUBLIC, PRIVATE, HIDE
    }
}
