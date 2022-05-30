package me.sample.gateway.dadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.config.gateway.DadataGatewayProperties;
import me.sample.config.gateway.ProxyGatewayProperties;
import me.sample.gateway.Gateways;
import me.sample.gateway.LoggingClientHttpRequestInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class DadataGateway {

    private static final String PATH_CITY_BY_IP = "/suggestions/api/4_1/rs/iplocate/address";
    private static final String QUERY_PARAM_IP = "ip";

    private static final String PATH_CITY_BY_COORDINATES = "/suggestions/api/4_1/rs/geolocate/address";
    private static final String QUERY_PARAM_LAT = "lat";
    private static final String QUERY_PARAM_LON = "lon";
    private static final String QUERY_PARAM_RADIUS = "radius_meters";

    private static final Long DEFAULT_CITY_SEARCH_RADIUS = 1000L;


    RestTemplate restTemplate;

    String baseUrl;
    HttpHeaders authHeaders;

    public DadataGateway(RestTemplateBuilder restTemplateBuilder,
                         DadataGatewayErrorHandler dadataGatewayErrorHandler,
                         ProxyGatewayProperties proxyGatewayProperties,
                         DadataGatewayProperties dadataGatewayProperties) {
        this.baseUrl = dadataGatewayProperties.getApi().getUrl();

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.set(HttpHeaders.AUTHORIZATION, String.format("Token %s", dadataGatewayProperties.getApi().getAuth().getToken()));
        this.authHeaders = authHeaders;

        RestTemplate restTemplate = restTemplateBuilder
                .additionalInterceptors(new LoggingClientHttpRequestInterceptor())
                .errorHandler(dadataGatewayErrorHandler)
                .build();

        if (proxyGatewayProperties.getEnabled()) {
            Gateways.configureProxy(restTemplate, proxyGatewayProperties);
        }

        this.restTemplate = restTemplate;
    }

    @HystrixCommand(
            fallbackMethod = "findFallbackCityByIp",
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000"),
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50"),
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "5000")

            })
    public Optional<String> findCityByIp(String ip) {
        log.debug(".findCityByIp(ip: {})", ip);

        URI url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path(PATH_CITY_BY_IP)
                .queryParam(QUERY_PARAM_IP, ip)
                .build()
                .toUri();

        HttpEntity<?> request = new HttpEntity<>(authHeaders);

        String result = Optional.of(restTemplate.exchange(url, HttpMethod.GET, request, JsonNode.class))
                .filter((ResponseEntity<JsonNode> response) -> response.getStatusCode().is2xxSuccessful())
                .map(HttpEntity::getBody)
                .map((JsonNode node) -> node.get("location"))
                .map((JsonNode node) -> node.get("data"))
                .map((JsonNode node) -> node.get("city"))
                .map(JsonNode::asText)
                .orElse(null);

        log.debug(".findCityByIp(ip: {}): {}", ip, result);

        return Optional.ofNullable(result);
    }

    public Optional<String> findFallbackCityByIp(String ip, Throwable throwable) {
        log.debug(".findFallbackCityByIp(ip: {})", ip);

        log.error("Failed to find city by ip. Reason: {}", throwable.getMessage());

        return Optional.empty();
    }

    public Optional<String> findCityByCoordinates(double lat, double lon) {
        return findCityByCoordinates(lat, lon, DEFAULT_CITY_SEARCH_RADIUS);
    }

    private Optional<String> findCityByCoordinates(double lat, double lon, long radius) {
        log.debug(".findCityByCoordinates(lat: {}, lon: {}, radius: {})", lat, lon, radius);

        URI url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path(PATH_CITY_BY_COORDINATES)
                .queryParam(QUERY_PARAM_LAT, lat)
                .queryParam(QUERY_PARAM_LON, lon)
                .queryParam(QUERY_PARAM_RADIUS, radius)
                .build()
                .toUri();

        HttpEntity<?> request = new HttpEntity<>(authHeaders);

        String result = Optional.of(restTemplate.exchange(url, HttpMethod.GET, request, JsonNode.class))
                .filter((ResponseEntity<JsonNode> response) -> response.getStatusCode().is2xxSuccessful())
                .map(HttpEntity::getBody)
                .map((JsonNode node) -> node.get("suggestions"))
                .map((JsonNode node) -> node.get(0))
                .map((JsonNode node) -> node.get("data"))
                .map((JsonNode node) -> node.get("city"))
                .map(JsonNode::asText)
                .orElse(null);

        log.debug(".findCityByCoordinates(lat: {}, lon: {}, radius: {}): {}", lat, lon, radius, result);

        return Optional.ofNullable(result);
    }
}
