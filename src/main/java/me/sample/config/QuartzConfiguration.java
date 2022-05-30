package me.sample.config;

import me.sample.job.CampaignTriggerListener;
import org.quartz.spi.JobFactory;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfiguration {

    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext) {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBeanCustomizer setupSchedulerFactory(CampaignTriggerListener campaignTriggerListener) {
        return (SchedulerFactoryBean schedulerFactoryBean) ->
                schedulerFactoryBean.setGlobalTriggerListeners(campaignTriggerListener);
    }
}
