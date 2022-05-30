package me.sample;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.mapper.TypeRef;
import me.sample.repository.PropertyRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.config.NoSecurityConfiguration;
import me.sample.dto.PropertyDTO;
import me.sample.domain.Property;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@Import(NoSecurityConfiguration.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PropertyRestIntegrationTest {

    private static final String PATH_API_PROPERTY = "/api/properties";

    @LocalServerPort
    private int port;

    @Autowired
    private PropertyRepository propertyRepository;

    @Before
    public void setUp() throws Exception {
        propertyRepository.deleteAll();
    }

    @Test
    public void shouldIndexProperties() throws Exception {
        Property property = propertyRepository.save(Property.builder()
                .type(Property.Type.STRING)
                .name(String.valueOf(UUID.randomUUID()))
                .value(String.valueOf(UUID.randomUUID()))
                .description(String.valueOf(UUID.randomUUID()))
                .build());

        List<PropertyDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_PROPERTY)
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<PropertyDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(PropertyDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds.size(), is(response.size()));
        assertThat(responseIds, hasItem(property.getId()));
    }

    @Test
    public void shouldShowProperty() throws Exception {
        Property property = propertyRepository.save(Property.builder()
                .type(Property.Type.STRING)
                .name(String.valueOf(UUID.randomUUID()))
                .value(String.valueOf(UUID.randomUUID()))
                .description(String.valueOf(UUID.randomUUID()))
                .build());

        PropertyDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_PROPERTY + "/{id}", property.getId())
                .then().log().all()
                .statusCode(200)
                .extract().as(PropertyDTO.class);

        assertThat(response.getId(), is(property.getId()));
    }

    @Test
    public void shouldSaveProperty() throws Exception {
        PropertyDTO request = PropertyDTO.builder()
                .type(Property.Type.STRING)
                .name(String.valueOf(UUID.randomUUID()))
                .value(String.valueOf(UUID.randomUUID()))
                .description(String.valueOf(UUID.randomUUID()))
                .build();

        PropertyDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(PATH_API_PROPERTY)
                .then().log().all()
                .statusCode(201)
                .extract().as(PropertyDTO.class);

        assertThat(response.getId(), is(notNullValue()));

        assertThat(response.getType(), is(request.getType()));
        assertThat(response.getName(), is(request.getName()));
        assertThat(response.getValue(), is(request.getValue()));
        assertThat(response.getDescription(), is(request.getDescription()));

        Property found = propertyRepository.findById(response.getId())
                .orElseThrow(AssertionError::new);

        assertThat(found.getType(), is(request.getType()));
        assertThat(found.getName(), is(request.getName()));
        assertThat(found.getValue(), is(request.getValue()));
        assertThat(found.getDescription(), is(request.getDescription()));
    }

    @Test
    public void shouldUpdateProperty() throws Exception {
        Property property = propertyRepository.save(Property.builder()
                .type(Property.Type.STRING)
                .name(String.valueOf(UUID.randomUUID()))
                .value(String.valueOf(UUID.randomUUID()))
                .description(String.valueOf(UUID.randomUUID()))
                .build());

        PropertyDTO request = PropertyDTO.builder()
                .id(property.getId())
                .type(Property.Type.STRING)
                .value(String.valueOf(UUID.randomUUID()))
                .description(String.valueOf(UUID.randomUUID()))
                .build();

        PropertyDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .put(PATH_API_PROPERTY)
                .then().log().all()
                .statusCode(200)
                .extract().as(PropertyDTO.class);

        assertThat(response.getId(), is(property.getId()));

        assertThat(response.getType(), is(request.getType()));
        assertThat(response.getValue(), is(request.getValue()));
        assertThat(response.getDescription(), is(request.getDescription()));

        Property found = propertyRepository.findById(property.getId())
                .orElseThrow(AssertionError::new);

        assertThat(found.getType(), is(request.getType()));
        assertThat(found.getValue(), is(request.getValue()));
        assertThat(found.getDescription(), is(request.getDescription()));
    }

    @Test
    public void shouldDeleteProperty() throws Exception {
        Property property = propertyRepository.save(Property.builder()
                .type(Property.Type.STRING)
                .name(String.valueOf(UUID.randomUUID()))
                .value(String.valueOf(UUID.randomUUID()))
                .description(String.valueOf(UUID.randomUUID()))
                .build());

        UUID responseId = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .delete(PATH_API_PROPERTY + "/{id}", property.getId())
                .then().log().all()
                .statusCode(200)
                .extract().as(UUID.class);

        assertThat(propertyRepository.findById(responseId),
                is(Optional.empty()));
    }
}
