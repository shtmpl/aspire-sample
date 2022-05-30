package me.sample.config;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.quartz.Job;
import org.quartz.SchedulerContext;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {

    ApplicationContext ctx;

    SchedulerContext schedulerContext;

    @Override
    public void setApplicationContext(final ApplicationContext context) {
        this.ctx = context;
    }

    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
        Job job = ctx.getBean(bundle.getJobDetail().getJobClass());
        BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(job);
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.addPropertyValues(bundle.getJobDetail().getJobDataMap());
        pvs.addPropertyValues(bundle.getTrigger().getJobDataMap());
        if (this.schedulerContext != null) {
            pvs.addPropertyValues(this.schedulerContext);
        }
        bw.setPropertyValues(pvs, true);
        return job;
    }

    @Override
    public void setSchedulerContext(SchedulerContext schedulerContext) {
        this.schedulerContext = schedulerContext;
        super.setSchedulerContext(schedulerContext);
    }
}
