package me.sample.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import lombok.experimental.FieldDefaults;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TerminalSearchDTO {

    @Singular
    List<UUID> ids = new LinkedList<>();

    @Singular
    List<String> hardwareIds = new LinkedList<>();

    @Singular
    List<String> vendors = new LinkedList<>();

    @Singular
    List<String> models = new LinkedList<>();

    @Singular
    List<String> osVersions = new LinkedList<>();

    @Singular
    List<String> clientIds = new LinkedList<>();
}
