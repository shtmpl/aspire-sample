package me.sample.config;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import me.sample.config.gateway.ProxyGatewayProperties;
import me.sample.gateway.Gateways;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Configuration
public class FirebaseConfiguration {

    ProxyGatewayProperties proxyGatewayProperties;

    @Bean
    @SneakyThrows(IOException.class)
    public FirebaseMessaging firebaseMessaging(@Value("${push.notification.GOOGLE_APP_CREDENTIALS}")
                                                       String googleAppCredentials) {
        try (InputStream in = Files.newInputStream(Paths.get(googleAppCredentials))) {
            FirebaseOptions options;
            if (Boolean.TRUE.equals(proxyGatewayProperties.getEnabled())) {
                HttpTransport transport = createHttpTransport();
                options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(in, () -> transport))
                        .setHttpTransport(transport)
                        .build();
            } else {
                options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(in))
                        .build();
            }

            FirebaseApp.initializeApp(options);
            return FirebaseMessaging.getInstance();
        }
    }

    private HttpTransport createHttpTransport() {
        return new NetHttpTransport.Builder()
                .setProxy(Gateways.createProxy(proxyGatewayProperties))
                .build();
    }
}
