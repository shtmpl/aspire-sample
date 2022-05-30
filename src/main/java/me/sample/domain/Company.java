package me.sample.domain;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
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
@Table(name = "company")
@Entity
public class Company extends AbstractIdentifiable<UUID> {

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

    String name;

    String description;

    @Builder.Default
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<CompanyAuthority> authorities = new LinkedHashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "company")
    Set<Partner> partners = new LinkedHashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "company")
    Set<Application> applications = new LinkedHashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "company")
    Set<Category> categories = new LinkedHashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "company")
    Set<Campaign> campaigns = new LinkedHashSet<>();
}
