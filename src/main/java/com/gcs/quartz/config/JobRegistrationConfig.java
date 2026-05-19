package com.gcs.quartz.config;

import com.gcs.quartz.job.LoggingJob;
import com.gcs.quartz.model.JobDefinition;
import com.gcs.quartz.registry.JobRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Configuration for registering all Quartz jobs
 * Add new jobs here as they are created
 */
@Component
public class JobRegistrationConfig {

    private final JobRegistry jobRegistry;

    public JobRegistrationConfig(JobRegistry jobRegistry) {
        this.jobRegistry = jobRegistry;
    }

    /**
     * Register all jobs after bean initialization
     * This is the place to add new jobs to the application
     */
    @PostConstruct
    public void registerAllJobs() {
        // Register LoggingJob
        registerLoggingJob();

        // TODO: Register additional jobs here as needed
        // Example:
        // registerEmailNotificationJob();
        // registerDataSyncJob();
        // etc.
    }

    /**
     * Register the LoggingJob
     */
    private void registerLoggingJob() {
        JobDefinition loggingJobDef = new JobDefinition(
                "LoggingJob",                              // Job Name
                "WeekdayGroup",                            // Job Group
                "LoggingTrigger",                          // Trigger Name
                "WeekdayGroup",                            // Trigger Group
                "0 0/30 * ? * MON-FRI",                    // Cron Expression
                "Job that logs messages every 30 minutes on weekdays",  // Description
                LoggingJob.class,                          // Job Class
                true                                       // Enabled
        );

        jobRegistry.registerJob(loggingJobDef);
    }

    // TODO: Add registration methods for new jobs here
    // Example 1: Register Email Notification Job
    // private void registerEmailNotificationJob() {
    //     JobDefinition emailJobDef = new JobDefinition(
    //             "EmailNotificationJob",
    //             "EmailGroup",
    //             "EmailTrigger",
    //             "EmailGroup",
    //             "0 0 9 * * ?",
    //             "Job that sends email notifications",
    //             EmailNotificationJob.class,
    //             true
    //     );
    //     jobRegistry.registerJob(emailJobDef);
    // }

    // Example 2: Register Data Sync Job
    // private void registerDataSyncJob() {
    //     JobDefinition syncJobDef = new JobDefinition(
    //             "DataSyncJob",
    //             "SyncGroup",
    //             "SyncTrigger",
    //             "SyncGroup",
    //             "0 */15 * * * ?",
    //             "Job that synchronizes data",
    //             DataSyncJob.class,
    //             true
    //     );
    //     jobRegistry.registerJob(syncJobDef);
    // }
}




