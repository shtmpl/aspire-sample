package me.sample.config.gateway;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "gateway.dadata", ignoreUnknownFields = false)
public class DadataGatewayProperties {

    Api api;

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Api {

        @NotBlank
        String url;

        @NotNull
        Auth auth;

        @FieldDefaults(level = AccessLevel.PRIVATE)
        @NoArgsConstructor
        @AllArgsConstructor
        @Getter
        @Setter
        public static class Auth {

            @NotNull
            String token;
        }
    }
}
