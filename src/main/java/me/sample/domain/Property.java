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

import javax.persistence.*;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Getter
@Setter
@Table(name = "property")
@Entity
public class Property extends AbstractIdentifiable<UUID> {

    public enum Type {
        BOOL, STRING, INT, DOUBLE
    }

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    UUID id;

    @Enumerated(EnumType.STRING)
    Type type;

    String name;

    String value;

    String description;

    public Boolean getBooleanValue() {
        if (type != Property.Type.BOOL) {
            throw new RuntimeException(String.format("Property: %s is not of a type: %s", name, Type.BOOL));
        }

        return value == null ? null : Boolean.valueOf(value);
    }

    public Integer getIntegerValue() {
        if (type != Property.Type.INT) {
            throw new RuntimeException(String.format("Property: %s is not of a type: %s", name, Type.INT));
        }

        return value == null ? null : Integer.valueOf(value);
    }

    public Double getDoubleValue() {
        if (type != Property.Type.DOUBLE) {
            throw new RuntimeException(String.format("Property: %s is not of a type: %s", name, Type.DOUBLE));
        }

        return value == null ? null : Double.valueOf(value);
    }

    public String getStringValue() {
        if (type != Type.STRING) {
            throw new RuntimeException(String.format("Property: %s is not of a type: %s", name, Type.STRING));
        }

        return value;
    }
}
