package me.sample.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.GeoPositionInfo;
import me.sample.domain.geo.Cluster;
import me.sample.domain.geo.ClusterGeoposition;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ClusterGeopositionRepositoryIntegrationTest {

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private GeoPositionInfoRepository geoPositionInfoRepository;

    @Autowired
    private ClusterGeopositionRepository clusterGeopositionRepository;

    @Before
    public void setUp() throws Exception {
        clusterGeopositionRepository.deleteAll();
    }

    @Test
    public void shouldSaveClusterGeoposition() throws Exception {
        Cluster cluster = clusterRepository.save(Cluster.builder()
                .lat(42.0)
                .lon(42.0)
                .build());

        GeoPositionInfo geoposition = geoPositionInfoRepository.save(GeoPositionInfo.builder()
                .lat(42.0)
                .lon(42.0)
                .build());

        ClusterGeoposition result = clusterGeopositionRepository.save(ClusterGeoposition.builder()
                .cluster(cluster)
                .geoposition(geoposition)
                .accepted(true)
                .build());

        assertThat(result.getId().getClusterId(), is(cluster.getId()));
        assertThat(result.getId().getGeopositionId(), is(geoposition.getId()));
    }
}
