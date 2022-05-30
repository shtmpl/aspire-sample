package me.sample.gateway;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.gateway.dadata.DadataGateway;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DadataGatewayIntegrationTest {

    @Autowired
    private DadataGateway dadataGateway;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldFindCityByIp() throws Exception {
        assertThat(dadataGateway.findCityByIp("46.226.227.2"),
                is(Optional.of("Краснодар")));
    }

    @Test
    public void shouldFindCityByCoordinates() throws Exception {
        assertThat(dadataGateway.findCityByCoordinates(55.878, 37.653),
                is(Optional.of("Москва")));

        assertThat(dadataGateway.findCityByCoordinates(55.4314428, 37.2714636),
                is(Optional.of("Москва")));
        assertThat(dadataGateway.findCityByCoordinates(55.633812, 37.439706),
                is(Optional.of("Москва")));
        assertThat(dadataGateway.findCityByCoordinates(60.9601007, 76.5325456),
                is(Optional.of("Нижневартовск")));
    }
}
