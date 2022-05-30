package me.sample.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.Id;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Entity
public class PosFrequency {
    @JsonIgnore
    @Id
    String id;
    Integer count;
    Integer hour;
    Integer day;
    Integer week;
    Integer month;
    Integer year;


}
