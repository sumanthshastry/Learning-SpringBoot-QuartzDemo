package com.shastry.learning.quartz.QuartzDemo.config;

import com.shastry.learning.quartz.QuartzDemo.scheduled.OneMinuteJob;
import com.shastry.learning.quartz.QuartzDemo.scheduled.ThirtySecondJob;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

@Configuration
@AllArgsConstructor
public class SchedulerConfig {

    @Component
    @Data
    @ConfigurationProperties("app.scheduling")
    public static class SchedulerConfigProperties {

        @Data
        public static class ScheduledJobTimingConfig {
            private String cron;
            private int startDelayMillis;
            private int fixedDelay;
        }

        private ScheduledJobTimingConfig oneMinuteJob;
        private ScheduledJobTimingConfig thirtySecondJob;

    }

    private SchedulerConfigProperties schedulerConfigProperties;
    private final QuartzProperties quartzProperties;

    // --------------------- Jobs -----------------------------

    @Bean("oneMinuteJob")
    public JobDetailFactoryBean oneMinuteJob() {
        return createJobDetail(OneMinuteJob.class, "One Minute Job");
    }

    @Bean("thirtySecondJob")
    public JobDetailFactoryBean thirtySecondJob() {
        return createJobDetail(ThirtySecondJob.class, "Thirty Second Job");
    }

    // --------------------- Triggers  ------------------------

    @Bean("thirtySecondJobTrigger")
    public SimpleTriggerFactoryBean thirtySecondJobTrigger(@Qualifier("thirtySecondJob") JobDetail jobDetail) {
        var trigger = new SimpleTriggerFactoryBean();
        trigger.setJobDetail(jobDetail);
        trigger.setName(jobDetail.getDescription());
        trigger.setStartDelay(schedulerConfigProperties.getThirtySecondJob().getStartDelayMillis());
        trigger.setRepeatInterval(schedulerConfigProperties.getThirtySecondJob().getFixedDelay());
        return trigger;
    }

    @Bean("oneMinuteJobTrigger")
    public CronTriggerFactoryBean oneMinuteJobTrigger(@Qualifier("oneMinuteJob") JobDetail jobDetail) {
        return createCronTrigger(jobDetail,
            schedulerConfigProperties.getOneMinuteJob().getCron(),
            schedulerConfigProperties.getOneMinuteJob().getStartDelayMillis());
    }

    @Bean
    @DependsOn("quartzDataSourceInitializer")
    public SchedulerFactoryBean schedulerFactoryBean(
        List<JobDetail> jobDetails,
        List<Trigger> triggers,
        DataSource dataSource,
        DataSourceTransactionManager transactionManager) {

        var props = new Properties();
        var factory = new SchedulerFactoryBean();

        props.putAll(quartzProperties.getProperties());
        factory.setQuartzProperties(props);
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setJobFactory(autowiringSpringBeanJobFactory());
        factory.setJobDetails(jobDetails.toArray(new JobDetail[0]));
        factory.setTriggers(triggers.toArray(new Trigger[0]));
        factory.setAutoStartup(true);
        factory.setOverwriteExistingJobs(true);

        return factory;
    }

    @Bean
    public QuartzDataSourceScriptDatabaseInitializer quartzDataSourceInitializer(DataSource dataSource) {
        return new QuartzDataSourceScriptDatabaseInitializer(dataSource, quartzProperties);
    }

    @Bean
    public AutowiringSpringBeanJobFactory autowiringSpringBeanJobFactory() {
        return new AutowiringSpringBeanJobFactory();
    }


    private <T extends Job> JobDetailFactoryBean createJobDetail(Class<T> jobClass, String jobName) {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(jobClass);
        jobDetailFactoryBean.setDescription(jobName);
        jobDetailFactoryBean.setDurability(true);
        return jobDetailFactoryBean;
    }

    private CronTriggerFactoryBean createCronTrigger(JobDetail jobDetail, String cron, int startDelayMillis) {
        CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
        cronTriggerFactoryBean.setJobDetail(jobDetail);
        cronTriggerFactoryBean.setCronExpression(cron);
        cronTriggerFactoryBean.setStartDelay(startDelayMillis);
        cronTriggerFactoryBean.setName(jobDetail.getDescription());
        return cronTriggerFactoryBean;
    }
}
