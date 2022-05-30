package me.sample.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StoreImportDTO {
    UUID id;
    String name;
    String city;
    String address;
    CoordinatesDTO coordinates;
    List<String> phones;
    UUID partnerId;
    String kladrId;
    String fiasId;
}
