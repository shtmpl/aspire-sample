package me.sample.gateway.bank;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.sample.config.gateway.ProxyGatewayProperties;
import me.sample.config.gateway.BankGatewayProperties;
import me.sample.gateway.GatewayException;
import me.sample.gateway.Gateways;
import me.sample.gateway.LoggingClientHttpRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import me.sample.gateway.bank.response.LayoutPage;
import me.sample.gateway.bank.response.ResponseCategory;
import me.sample.gateway.bank.response.ResponsePartner;
import me.sample.gateway.bank.response.ResponsePromo;
import me.sample.gateway.bank.response.ResponseShop;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Клиент для осуществления взаимодействия с API банка
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class BankGateway {

    private static final String PATH_CATEGORY = "/public-api/v4/data/categories";
    private static final String QUERY_PARAM_CATEGORY_ID = "categoryId";

    private static final String PATH_PARTNER = "/public-api/v4/data/partners";
    private static final String QUERY_PARAM_PARTNER_ID = "partnerId";

    private static final String PATH_SHOP = "/public-api/v4/data/shops";
    private static final String QUERY_PARAM_SHOP_ID = "shopId";

    private static final String PATH_PROMO = "/public-api/v4/data/promos";
    private static final String QUERY_PARAM_PROMO_ID = "promoId";

    private static final String QUERY_PARAM_PAGE_NUMBER = "pageNumber";
    private static final String QUERY_PARAM_PAGE_SIZE = "pageSize";

    RestTemplate restTemplate;

    String baseUrl;

    @Autowired
    public BankGateway(RestTemplateBuilder restTemplateBuilder,
                       ProxyGatewayProperties proxyGatewayProperties,
                       BankGatewayProperties bankGatewayProperties) {
        this.baseUrl = bankGatewayProperties.getApi().getUrl();

        RestTemplate restTemplate = restTemplateBuilder
                .additionalInterceptors(new LoggingClientHttpRequestInterceptor())
                .basicAuthentication(
                        bankGatewayProperties.getApi().getAuth().getUsername(),
                        bankGatewayProperties.getApi().getAuth().getPassword())
                .errorHandler(new BankGatewayErrorHandler())
                .build();

        if (proxyGatewayProperties.getEnabled()) {
            Gateways.configureProxy(restTemplate, proxyGatewayProperties);
        }

        this.restTemplate = restTemplate;
    }

    public LayoutPage<ResponseCategory> indexCategories(Pageable pageable) {
        RequestEntity<?> request = RequestEntity
                .get(UriComponentsBuilder.fromHttpUrl(baseUrl)
                        .path(PATH_CATEGORY)
                        .queryParam(QUERY_PARAM_PAGE_NUMBER, pageable.getPageNumber())
                        .queryParam(QUERY_PARAM_PAGE_SIZE, pageable.getPageSize())
                        .build()
                        .toUri())
                .build();

        ResponseEntity<LayoutPage<ResponseCategory>> response = restTemplate
                    .exchange(request, new ParameterizedTypeReference<LayoutPage<ResponseCategory>>() {
                    });

        return response.getBody();
    }

    public Optional<ResponseCategory> showCategory(UUID id) {
        RequestEntity<?> request = RequestEntity
                .get(UriComponentsBuilder.fromHttpUrl(baseUrl)
                        .path(PATH_CATEGORY)
                        .queryParam(QUERY_PARAM_CATEGORY_ID, id)
                        .build()
                        .toUri())
                .build();

        ResponseEntity<LayoutPage<ResponseCategory>> response = restTemplate
                    .exchange(request, new ParameterizedTypeReference<LayoutPage<ResponseCategory>>() {
                    });

        LayoutPage<ResponseCategory> page = response.getBody();
        if (page == null) {
            throw new GatewayException("No response body received");
        }

        List<ResponseCategory> data = page.getData();
        if (data == null) {
            return Optional.empty();
        }

        return data.stream().findFirst();
    }

    public LayoutPage<ResponsePartner> indexPartners(Pageable pageable) {
        RequestEntity<?> request = RequestEntity
                .get(UriComponentsBuilder.fromHttpUrl(baseUrl)
                        .path(PATH_PARTNER)
                        .queryParam(QUERY_PARAM_PAGE_NUMBER, pageable.getPageNumber())
                        .queryParam(QUERY_PARAM_PAGE_SIZE, pageable.getPageSize())
                        .build()
                        .toUri())
                .build();

        ResponseEntity<LayoutPage<ResponsePartner>> response = restTemplate
                    .exchange(request, new ParameterizedTypeReference<LayoutPage<ResponsePartner>>() {
                    });

        return response.getBody();
    }

    public Optional<ResponsePartner> showPartner(UUID id) {
        RequestEntity<?> request = RequestEntity
                .get(UriComponentsBuilder.fromHttpUrl(baseUrl)
                        .path(PATH_PARTNER)
                        .queryParam(QUERY_PARAM_PARTNER_ID, id)
                        .build()
                        .toUri())
                .build();

        ResponseEntity<LayoutPage<ResponsePartner>> response = restTemplate
                    .exchange(request, new ParameterizedTypeReference<LayoutPage<ResponsePartner>>() {
                    });

        LayoutPage<ResponsePartner> page = response.getBody();
        if (page == null) {
            throw new GatewayException("No response body received");
        }

        List<ResponsePartner> data = page.getData();
        if (data == null) {
            return Optional.empty();
        }

        return data.stream().findFirst();
    }

    public LayoutPage<ResponseShop> indexShops(Pageable pageable) {
        RequestEntity<?> request = RequestEntity
                .get(UriComponentsBuilder.fromHttpUrl(baseUrl)
                        .path(PATH_SHOP)
                        .queryParam(QUERY_PARAM_PAGE_NUMBER, pageable.getPageNumber())
                        .queryParam(QUERY_PARAM_PAGE_SIZE, pageable.getPageSize())
                        .build()
                        .toUri())
                .build();

        ResponseEntity<LayoutPage<ResponseShop>> response = restTemplate
                    .exchange(request, new ParameterizedTypeReference<LayoutPage<ResponseShop>>() {
                    });

        return response.getBody();
    }

    public Optional<ResponseShop> showShop(UUID id) {
        RequestEntity<?> request = RequestEntity
                .get(UriComponentsBuilder.fromHttpUrl(baseUrl)
                        .path(PATH_SHOP)
                        .queryParam(QUERY_PARAM_SHOP_ID, id)
                        .build()
                        .toUri())
                .build();

        ResponseEntity<LayoutPage<ResponseShop>> response = restTemplate
                    .exchange(request, new ParameterizedTypeReference<LayoutPage<ResponseShop>>() {
                    });

        LayoutPage<ResponseShop> page = response.getBody();
        if (page == null) {
            throw new GatewayException("No response body received");
        }

        List<ResponseShop> data = page.getData();
        if (data == null) {
            return Optional.empty();
        }

        return data.stream().findFirst();
    }

    public LayoutPage<ResponsePromo> indexPromos(Pageable pageable) {
        RequestEntity<?> request = RequestEntity
                .get(UriComponentsBuilder.fromHttpUrl(baseUrl)
                        .path(PATH_PROMO)
                        .queryParam(QUERY_PARAM_PAGE_NUMBER, pageable.getPageNumber())
                        .queryParam(QUERY_PARAM_PAGE_SIZE, pageable.getPageSize())
                        .build()
                        .toUri())
                .build();

        ResponseEntity<LayoutPage<ResponsePromo>> response = restTemplate
                    .exchange(request, new ParameterizedTypeReference<LayoutPage<ResponsePromo>>() {
                    });

        return response.getBody();
    }

    public Optional<ResponsePromo> showPromo(UUID id) {
        RequestEntity<?> request = RequestEntity
                .get(UriComponentsBuilder.fromHttpUrl(baseUrl)
                        .path(PATH_PROMO)
                        .queryParam(QUERY_PARAM_PROMO_ID, id)
                        .build()
                        .toUri())
                .build();

        ResponseEntity<LayoutPage<ResponsePromo>> response = restTemplate
                    .exchange(request, new ParameterizedTypeReference<LayoutPage<ResponsePromo>>() {
                    });

        LayoutPage<ResponsePromo> page = response.getBody();
        if (page == null) {
            throw new GatewayException("No response body received");
        }

        List<ResponsePromo> data = page.getData();
        if (data == null) {
            return Optional.empty();
        }

        return data.stream().findFirst();
    }
}
