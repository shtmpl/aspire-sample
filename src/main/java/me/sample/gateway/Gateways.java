package me.sample.gateway;

import me.sample.config.gateway.ProxyGatewayProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.EnumSet;

public final class Gateways {

    public static void configureProxy(RestTemplate restTemplate, ProxyGatewayProperties proxyGatewayProperties) {
        String proxyType = proxyGatewayProperties.getType();
        String proxyHost = proxyGatewayProperties.getHost();
        Integer proxyPort = proxyGatewayProperties.getPort();
        Integer proxyConnectionTimeout = proxyGatewayProperties.getConnectionTimeout();
        Integer proxyReadTimeout = proxyGatewayProperties.getReadTimeout();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setProxy(new Proxy(proxyTypeOf(proxyType), new InetSocketAddress(proxyHost, proxyPort)));
        requestFactory.setConnectTimeout(proxyConnectionTimeout);
        requestFactory.setReadTimeout(proxyReadTimeout);

        restTemplate.setRequestFactory(requestFactory);
    }

    public static Proxy createProxy(ProxyGatewayProperties proxyGatewayProperties) {
        return new Proxy(
                proxyTypeOf(proxyGatewayProperties.getType()),
                new InetSocketAddress(
                        proxyGatewayProperties.getHost(),
                        proxyGatewayProperties.getPort()));
    }

    public static Proxy.Type proxyTypeOf(String type) {
        return EnumSet.allOf(Proxy.Type.class)
                .stream()
                .filter((Proxy.Type it) -> it.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(String.format("No proxy type defined for name: %s", type)));
    }
}
