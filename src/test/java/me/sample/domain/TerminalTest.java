package me.sample.domain;

import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TerminalTest {

    @Test
    public void shouldMatchTerminalCity() throws Exception {
        assertThat(Terminal.builder()
                        .city("X")
                        .build()
                        .matches(Stream.of(
                                PropFilter.builder()
                                        .sign(PropFilter.Sign.EQUAL)
                                        .name("city")
                                        .value("X")
                                        .build())
                                .collect(Collectors.toList())),
                is(true));
    }

    @Test
    public void shouldMatchTerminalNumericProps() throws Exception {
        assertThat(Terminal.builder()
                        .build()
                        .setProp("eq", 10L)
                        .setProp("le", 19)
                        .setProp("l", 18)
                        .setProp("ge", 0.5)
                        .setProp("g", 301L)
                        .matches(Stream.of(
                                PropFilter.builder()
                                        .name("eq")
                                        .sign(PropFilter.Sign.EQUAL)
                                        .value(10)
                                        .build(),
                                PropFilter.builder()
                                        .name("le")
                                        .sign(PropFilter.Sign.LESS_OR_EQUAL)
                                        .value(20L)
                                        .build(),
                                PropFilter.builder()
                                        .name("l")
                                        .sign(PropFilter.Sign.LESS_OR_EQUAL)
                                        .value(22.2)
                                        .build(),
                                PropFilter.builder()
                                        .name("ge")
                                        .sign(PropFilter.Sign.GREATER_OR_EQUAL)
                                        .value(-1)
                                        .build(),
                                PropFilter.builder()
                                        .name("g")
                                        .sign(PropFilter.Sign.GREATER)
                                        .value(300)
                                        .build())
                                .collect(Collectors.toList())),
                is(true));
    }

    @Test
    public void shouldMatchTerminalStringProps() throws Exception {
        assertThat(Terminal.builder()
                        .build()
                        .setProp("eq", "string")
                        .setProp("le", "19")
                        .setProp("l", "18")
                        .setProp("ge", "2")
                        .setProp("g", "301")
                        .matches(Stream.of(
                                PropFilter.builder()
                                        .name("eq")
                                        .sign(PropFilter.Sign.EQUAL)
                                        .value("string")
                                        .build(),
                                PropFilter.builder()
                                        .name("le")
                                        .sign(PropFilter.Sign.LESS_OR_EQUAL)
                                        .value("20")
                                        .build(),
                                PropFilter.builder()
                                        .name("l")
                                        .sign(PropFilter.Sign.LESS_OR_EQUAL)
                                        .value("22")
                                        .build(),
                                PropFilter.builder()
                                        .name("ge")
                                        .sign(PropFilter.Sign.GREATER_OR_EQUAL)
                                        .value("1")
                                        .build(),
                                PropFilter.builder()
                                        .name("g")
                                        .sign(PropFilter.Sign.GREATER)
                                        .value("300")
                                        .build())
                                .collect(Collectors.toList())),
                is(true));
    }

    @Test
    public void shouldThrowWhenMatchingUnsupportedPropType() throws Exception {
        try {
            Terminal.builder()
                    .build()
                    .setProp("eq", new Object())
                    .matches(Stream.of(
                            PropFilter.builder()
                                    .name("eq")
                                    .sign(PropFilter.Sign.EQUAL)
                                    .value(new Object())
                                    .build())
                            .collect(Collectors.toList()));

            fail();
        } catch (UnsupportedOperationException expected) {
            System.out.printf("Thrown: %s%n", expected);
        }
    }
}
