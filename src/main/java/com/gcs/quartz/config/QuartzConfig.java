package com.gcs.quartz.config;

import com.gcs.quartz.factory.JobTriggerFactory;
import com.gcs.quartz.factory.QuartzJobFactory;
import com.gcs.quartz.registry.JobRegistry;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

@Configuration
public class QuartzConfig {

    /**
     * Configure the SchedulerFactoryBean with our custom job factory
     * to enable Spring dependency injection in Quartz jobs
     *
     * This bean now dynamically registers all jobs from the JobRegistry
     * and uses the provided DataSource for persistence.
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(QuartzJobFactory jobFactory,
                                                     JobTriggerFactory jobTriggerFactory,
                                                     DataSource dataSource) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setJobFactory(jobFactory);
        schedulerFactory.setDataSource(dataSource);

        // Configure Quartz properties
        Properties properties = new Properties();
        properties.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        properties.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
        properties.setProperty("org.quartz.jobStore.useProperties", "false");
        schedulerFactory.setQuartzProperties(properties);

        // Register all jobs and triggers dynamically from the registry
        Map<JobDetail, Trigger> jobsAndTriggers = jobTriggerFactory.createAllJobsAndTriggers();

        if (!jobsAndTriggers.isEmpty()) {
            JobDetail[] jobDetails = jobsAndTriggers.keySet().toArray(new JobDetail[0]);
            Trigger[] triggers = jobsAndTriggers.values().toArray(new Trigger[0]);

            schedulerFactory.setJobDetails(jobDetails);
            schedulerFactory.setTriggers(triggers);
        }

        return schedulerFactory;
    }
}

