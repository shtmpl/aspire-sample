package me.sample.gateway;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.gateway.bank.BankGateway;
import me.sample.gateway.bank.response.ResponseCategory;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BankGatewayIntegrationTest {

    @Autowired
    private BankGateway bankGateway;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldIndexCategories() throws Exception {
        List<ResponseCategory> data = bankGateway.indexCategories(PageRequest.of(0, 1)).getData();

        assertThat(data.size(), is(1));
    }
}
