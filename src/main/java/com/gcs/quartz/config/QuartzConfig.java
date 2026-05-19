package com.gcs.quartz.config;

import com.gcs.quartz.factory.JobTriggerFactory;
import com.gcs.quartz.factory.QuartzJobFactory;
import com.gcs.quartz.registry.JobRegistry;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Map;

@Configuration
public class QuartzConfig {

    /**
     * Configure the SchedulerFactoryBean with our custom job factory
     * to enable Spring dependency injection in Quartz jobs
     *
     * This bean now dynamically registers all jobs from the JobRegistry
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(QuartzJobFactory jobFactory,
                                                     JobTriggerFactory jobTriggerFactory,
                                                     JobRegistry jobRegistry) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setJobFactory(jobFactory);

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

