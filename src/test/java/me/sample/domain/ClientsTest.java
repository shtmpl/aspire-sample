package me.sample.domain;

import me.sample.Resources;
import org.junit.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ClientsTest {

    @Test
    public void shouldParsePositiveIntegerAsClientId() throws Exception {
        assertThat(Clients.parseClientId(null),
                is(nullValue()));

        assertThat(Clients.parseClientId(""),
                is(nullValue()));
        assertThat(Clients.parseClientId("    "),
                is(nullValue()));

        assertThat(Clients.parseClientId("Invalid"),
                is(nullValue()));

        assertThat(Clients.parseClientId("42"),
                is("42"));
        assertThat(Clients.parseClientId("\uFEFF42"),
                is("42"));
    }

    @Test
    public void shouldReadClientIdsFromXsl() throws Exception {
        try (InputStream in = Files.newInputStream(Resources.path("client/client-ids.xls"))) {
            List<String> results = Clients.readClientIdsFromXls(in);
            System.out.println(results);

            assertThat(results.size(), is(10));
        }
    }

    @Test
    public void shouldReadClientIdsFromXslx() throws Exception {
        try (InputStream in = Files.newInputStream(Resources.path("client/client-ids.xlsx"))) {
            List<String> results = Clients.readClientIdsFromXls(in);
            System.out.println(results);

            assertThat(results.size(), is(10));
        }
    }
}
