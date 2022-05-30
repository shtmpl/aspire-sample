package me.sample.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import me.sample.domain.Permission;

import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyAuthorityDto {
    Long userId;
    UUID companyId;
    Permission permission;
}
