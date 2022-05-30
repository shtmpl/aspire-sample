package me.sample.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.geo.Cluster;
import me.sample.domain.geo.ClusterSpecifications;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ClusterRepositoryIntegrationTest {

    @Autowired
    private ClusterRepository clusterRepository;

    @Before
    public void setUp() throws Exception {
        clusterRepository.deleteAll();
    }

    @Test
    public void shouldFindClustersWithinRadius() throws Exception {
        Cluster cluster = clusterRepository.save(Cluster.builder()
                .lat(42.0)
                .lon(42.0)
                .build());

        List<Cluster> clusters = clusterRepository.findAll(ClusterSpecifications.coordinatesWithinRadius(42.0, 42.0, 0L));

        assertThat(clusters.stream()
                        .map(Cluster::getId)
                        .collect(Collectors.toSet()),
                contains(cluster.getId()));
    }
}
