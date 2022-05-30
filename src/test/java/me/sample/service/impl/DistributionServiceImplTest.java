package me.sample.service.impl;

import me.sample.domain.Campaign;
import me.sample.domain.Distribution;
import me.sample.repository.DistributionRepository;
import me.sample.service.ApplicationService;
import me.sample.service.NotificationService;
import me.sample.service.SecurityService;
import me.sample.service.TerminalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import me.sample.mapper.DistributionMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistributionServiceImplTest {
    @Mock
    private DistributionRepository distributionRepository;
    @Mock
    private DistributionMapper distributionMapper;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private SecurityService securityService;
    @Mock
    private TerminalService terminalService;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private DistributionServiceImpl distributionService;

    @Test
    void exceedIterations() {
        // given
        UUID distributionId = UUID.randomUUID();
        Distribution distribution = Distribution.builder()
                .campaign(Campaign.builder()
                        .build())
                .id(distributionId)
                .build();

        // when
        when(distributionRepository.getOne(eq(distributionId))).thenReturn(distribution);

        // call
        distributionService.executeDissemination(distributionId);

        // then
        verifyZeroInteractions(terminalService, notificationService, distributionRepository);
    }
}
