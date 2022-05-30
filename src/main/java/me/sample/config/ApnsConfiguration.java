package me.sample.config;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.auth.ApnsSigningKey;
import com.eatthepath.pushy.apns.proxy.HttpProxyHandlerFactory;
import com.eatthepath.pushy.apns.proxy.ProxyHandlerFactory;
import com.eatthepath.pushy.apns.proxy.Socks5ProxyHandlerFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import me.sample.config.gateway.ProxyGatewayProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import me.sample.gateway.Gateways;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Configuration
public class ApnsConfiguration {

    Environment env;

    ProxyGatewayProperties proxyGatewayProperties;

    @Bean
    @SneakyThrows({IOException.class, NoSuchAlgorithmException.class, InvalidKeyException.class})
    public ApnsClient apnsClient(@Value("${push.notification.JWT_CERTIFICATE_FILE}")
                                              String pushNotificationJWTCertFile,
                                 @Value("${push.notification.JWT_TEAM_ID}")
                                              String pushNotificationJWTTeamId,
                                 @Value("${push.notification.JWT_KEY_ID}")
                                              String pushNotificationJWTKeyId) {
        ApnsClientBuilder result = new ApnsClientBuilder()
                .setApnsServer(env.acceptsProfiles(Profiles.of("dev", "local"))
                        ? ApnsClientBuilder.DEVELOPMENT_APNS_HOST
                        : ApnsClientBuilder.PRODUCTION_APNS_HOST)
                .setSigningKey(ApnsSigningKey.loadFromPkcs8File(new File(pushNotificationJWTCertFile),
                        pushNotificationJWTTeamId, pushNotificationJWTKeyId));
        if (Boolean.TRUE.equals(proxyGatewayProperties.getEnabled())) {
            result.setProxyHandlerFactory(createProxyHandlerFactory());
        }

        return result.build();
    }

    private ProxyHandlerFactory createProxyHandlerFactory() {
        Proxy.Type proxyType = Gateways.proxyTypeOf(proxyGatewayProperties.getType());
        SocketAddress socketAddress = new InetSocketAddress(
                proxyGatewayProperties.getHost(),
                proxyGatewayProperties.getPort());
        switch (proxyType) {
            case HTTP:
                return new HttpProxyHandlerFactory(socketAddress);
            case SOCKS:
                return new Socks5ProxyHandlerFactory(socketAddress);
            default:
                throw new RuntimeException(String.format("Undefined proxy type specified: %s", proxyType));
        }
    }
}
