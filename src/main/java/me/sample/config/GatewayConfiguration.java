package me.sample.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import me.sample.config.gateway.DadataGatewayProperties;
import me.sample.config.gateway.ProxyGatewayProperties;
import me.sample.config.gateway.BankGatewayProperties;

@Configuration
@EnableConfigurationProperties({
        ProxyGatewayProperties.class,
        DadataGatewayProperties.class,
        BankGatewayProperties.class
})
public class GatewayConfiguration {
}
