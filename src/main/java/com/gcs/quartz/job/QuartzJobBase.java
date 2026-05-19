package com.gcs.quartz.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

/**
 * Base interface for all Quartz jobs
 * All jobs should implement this interface
 */
public interface QuartzJobBase extends Job {

    /**
     * @return the name of the job
     */
    String getJobName();

    /**
     * @return the group name of the job
     */
    String getJobGroup();

    /**
     * @return the cron expression for this job
     */
    String getCronExpression();

    /**
     * @return whether this job is enabled
     */
    default boolean isEnabled() {
        return true;
    }
}

