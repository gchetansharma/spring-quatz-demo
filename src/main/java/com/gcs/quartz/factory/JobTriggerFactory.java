package com.gcs.quartz.factory;

import com.gcs.quartz.model.JobDefinition;
import com.gcs.quartz.registry.JobRegistry;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Factory for creating JobDetail and Trigger instances from job definitions
 */
@Component
public class JobTriggerFactory {

    private static final Logger logger = LoggerFactory.getLogger(JobTriggerFactory.class);
    private final JobRegistry jobRegistry;

    public JobTriggerFactory(JobRegistry jobRegistry) {
        this.jobRegistry = jobRegistry;
    }

    /**
     * Create a JobDetail from a JobDefinition
     */
    public JobDetail createJobDetail(JobDefinition jobDefinition) {
        return JobBuilder.newJob(jobDefinition.getJobClass())
                .withIdentity(jobDefinition.getJobName(), jobDefinition.getJobGroup())
                .withDescription(jobDefinition.getDescription())
                .storeDurably()
                .build();
    }

    /**
     * Create a CronTrigger from a JobDefinition
     */
    public Trigger createTrigger(JobDefinition jobDefinition, JobDetail jobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDefinition.getTriggerName(), jobDefinition.getTriggerGroup())
                .withDescription(jobDefinition.getDescription())
                .withSchedule(CronScheduleBuilder.cronSchedule(jobDefinition.getCronExpression()))
                .build();
    }

    /**
     * Create all job details and triggers from the registry
     */
    public Map<JobDetail, Trigger> createAllJobsAndTriggers() {
        Map<JobDetail, Trigger> jobsAndTriggers = new LinkedHashMap<>();
        Collection<JobDefinition> enabledJobs = jobRegistry.getEnabledJobs();

        for (JobDefinition jobDefinition : enabledJobs) {
            try {
                JobDetail jobDetail = createJobDetail(jobDefinition);
                Trigger trigger = createTrigger(jobDefinition, jobDetail);
                jobsAndTriggers.put(jobDetail, trigger);
                logger.info("Created job detail and trigger for: {} in group: {}",
                        jobDefinition.getJobName(), jobDefinition.getJobGroup());
            } catch (Exception e) {
                logger.error("Failed to create job/trigger for: {} in group: {}",
                        jobDefinition.getJobName(), jobDefinition.getJobGroup(), e);
            }
        }

        return jobsAndTriggers;
    }
}

