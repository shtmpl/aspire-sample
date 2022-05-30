package me.sample.domain;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "company_authority")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyAuthority {

    @Id
    @GeneratedValue
    UUID id;

    Long userId;

    @EqualsAndHashCode.Exclude
    @ManyToOne
    Company company;

    @EqualsAndHashCode.Exclude
    @CreationTimestamp
    @Column(name = "cdat", updatable = false)
    LocalDateTime createdDate;

    @EqualsAndHashCode.Exclude
    @UpdateTimestamp
    @Column(name = "udat")
    LocalDateTime updatedDate;

    @Enumerated(EnumType.STRING)
    Permission permission;
}

