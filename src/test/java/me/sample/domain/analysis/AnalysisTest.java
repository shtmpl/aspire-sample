package me.sample.domain.analysis;

import org.junit.Test;
import me.sample.domain.Analysis;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

public class AnalysisTest {

    @Test
    public void shouldComputeDistanceInKilometers() throws Exception {
        assertThat(Analysis.spatialDistanceInKilometers(42.0, 42.0, 43.0, 42.0),
                is(closeTo(111.194, 0.001)));
    }
}
