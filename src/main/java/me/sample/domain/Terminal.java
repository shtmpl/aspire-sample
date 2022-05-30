package me.sample.domain;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
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
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class),
        @TypeDef(name = "pgsql_enum", typeClass = PostgresqlEnumType.class)
})
@Table(name = "terminal")
@Entity
public class Terminal extends AbstractIdentifiable<UUID> {

    public static final String PROP_KEY_BIRTH_DATE = "birthdate";
    public static final String PROP_KEY_CLIENT_ID = "clientId";

    public static final String FILTER_KEY_PLATFORM = "platform";
    public static final String FILTER_KEY_CITY = "city";
    public static final String FILTER_KEY_CAMPAIGN_ID = "campaignId";


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

    @ManyToOne
    Application application;

    String hardwareId;

    @Type(
            type = "pgsql_enum",
            parameters = {
                    @Parameter(name = "enumClass", value = "me.sample.model.TerminalPlatform")
            })
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "terminal_platform_type")
    TerminalPlatform platform;

    String pushId;

    String vendor;

    String model;

    String osVersion;

    String appVersion;

    String msisdn;

    String ip;

    /**
     * Текущее местоположение терминала (город)
     */
    String city;

    /**
     * Дата/время последнего обновления города данного терминала (lookup из dadata по ip)
     * Используется для оптимизации кол-ва вызовов внешнего сервиса
     */
    LocalDateTime lastLocationUpdate;

    /**
     * Тестовый терминал?
     */
    Boolean test;

    @Builder.Default
    @Type(type = "jsonb")
    @Column(name = "props", columnDefinition = "jsonb")
    Map<String, Object> props = new LinkedHashMap<>();

    public Object getProp(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Arg: key is null");
        }

        return props.get(key);
    }

    public Terminal setProp(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Arg: key is null");
        }

        props.put(key, value);

        return this;
    }

    public boolean matches(Collection<PropFilter> filters) {
        if (filters == null) {
            return true;
        }

        return filters.stream()
                .allMatch((PropFilter filter) -> {
                    String key = filter.getName();
                    if (Terminal.FILTER_KEY_PLATFORM.equals(key)) {
                        return valueMatches(platform == null ? null : platform.name(), filter);
                    } else if (Terminal.FILTER_KEY_CITY.equals(key)) {
                        return valueMatches(city, filter);
                    } else {
                        return valueMatches(props.get(key), filter);
                    }
                });
    }

    private boolean valueMatches(Object value, PropFilter filter) {
        if (value == null) {
            return false;
        }

        if (value instanceof Number) {
            return numericValueMatches((Number) value, filter);
        } else if (value instanceof String) {
            return stringValueMatches((String) value, filter);
        }

        throw new UnsupportedOperationException(String.format(
                "Only Numbers and Strings are able to be compared. Attempt to compare: %s",
                value.getClass().getName()));

    }

    private boolean numericValueMatches(Number value, PropFilter filter) {
        PropFilter.Sign op = filter.getSign();
        if (op == null) {
            return false;
        }

        Double p = value.doubleValue();
        Double v = ((Number) filter.getValue()).doubleValue();

        switch (op) {
            case LESS:
                return p < v;
            case LESS_OR_EQUAL:
                return p <= v;
            case EQUAL:
                return p.equals(v);
            case GREATER_OR_EQUAL:
                return p >= v;
            case GREATER:
                return p > v;
            default:
                return false;
        }
    }

    private boolean stringValueMatches(String value, PropFilter filter) {
        PropFilter.Sign op = filter.getSign();
        if (op == null) {
            return false;
        }

        String v = (String) filter.getValue();

        switch (op) {
            case LESS:
                return value.compareTo(v) < 0;
            case LESS_OR_EQUAL:
                return value.compareTo(v) <= 0;
            case EQUAL:
                return value.equals(v);
            case GREATER_OR_EQUAL:
                return value.compareTo(v) >= 0;
            case GREATER:
                return value.compareTo(v) > 0;
            default:
                return false;
        }
    }
}
