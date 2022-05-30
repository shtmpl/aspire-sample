package me.sample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;


@Configuration
@EnableSwagger2
@PropertySource("classpath:swagger.properties")
public class SwaggerConfiguration {
    public static final String APPLICATION = "Application";
    public static final String TERMINAL = "Terminal";
    public static final String CAMPAIGN = "Campaign";
    public static final String PROMO = "Promo";
    public static final String CATEGORY = "Category";
    public static final String PARTNER = "Partner";
    public static final String STORE = "Store";
    public static final String NOTIFICATION = "Notification";
    public static final String NOTIFICATION_TEMPLATE = "NotificationTemplate";
    public static final String REQUEST_LOG = "RequestLog";
    public static final String GEOPOSITION = "Geoposition";
    public static final String ANALYSIS = "Analysis";

    private static final String APPLICATION_DESC = "Provides applications info";
    private static final String TERMINAL_DESC = "Endpoints collect data from terminal";
    private static final String CAMPAIGN_DESC = "Provides campaigns info";
    private static final String PROMO_DESC = "Provides promos info";
    private static final String CATEGORY_DESC = "Provides category info";
    private static final String PARTNER_DESC = "Provides partners info";
    private static final String STORE_DESC = "Provides stores info";
    private static final String NOTIFICATION_DESC = "Provides notifications info";
    private static final String NOTIFICATION_TEMPLATE_DESC = "Provides notification templates info";
    private static final String REQUEST_LOG_DESC = "Provides request logs info";
    private static final String GEOPOSITION_LOG_DESC = "Provides geoposition info";
    private static final String ANALYSIS_DESC = "Provides analysis";

    private static final String TITLE = "Sample REST API";
    private static final String DESCRIPTION = "Sample";
    private static final String VERSION = "0.1.0";
    private static final String TERMS_OF_SERVICE_URL = null;
    private static final Contact CONTACT = new Contact("", "", "");
    private static final String LICENSE = null;
    private static final String LICENSE_URL = null;

    @Bean
    public Docket webApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("Web")
                .select()
                .apis(RequestHandlerSelectors.basePackage("me.sample.web.rest"))
                .paths(PathSelectors.any())
                .build()
                .tags(new Tag(APPLICATION, APPLICATION_DESC),
                        new Tag(TERMINAL, TERMINAL_DESC),
                        new Tag(CAMPAIGN, CAMPAIGN_DESC),
                        new Tag(PROMO, PROMO_DESC),
                        new Tag(CATEGORY, CATEGORY_DESC),
                        new Tag(PARTNER, PARTNER_DESC),
                        new Tag(STORE, STORE_DESC),
                        new Tag(NOTIFICATION, NOTIFICATION_DESC),
                        new Tag(NOTIFICATION_TEMPLATE, NOTIFICATION_TEMPLATE_DESC),
                        new Tag(REQUEST_LOG, REQUEST_LOG_DESC),
                        new Tag(GEOPOSITION, GEOPOSITION_LOG_DESC),
                        new Tag(ANALYSIS, ANALYSIS_DESC))
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(TITLE, DESCRIPTION, VERSION, TERMS_OF_SERVICE_URL, CONTACT, LICENSE, LICENSE_URL, Collections.emptyList());
    }
}
