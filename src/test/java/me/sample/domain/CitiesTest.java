package me.sample.domain;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class CitiesTest {

    @Test
    public void shouldFormatCityName() throws Exception {
        assertThat(Cities.formatCityName(null),
                is(""));
        assertThat(Cities.formatCityName(""),
                is(""));
        assertThat(Cities.formatCityName("    "),
                is(""));
        assertThat(Cities.formatCityName("г."),
                is(""));

        assertThat(Cities.formatCityName("г.Санкт-Петербург"),
                is("Санкт-Петербург"));
        assertThat(Cities.formatCityName("г.  Санкт-Петербург  "),
                is("Санкт-Петербург"));

        assertThat(Cities.formatCityName("САНКТ-ПЕТЕРБУРГ"),
                is("Санкт-Петербург"));
    }
}
