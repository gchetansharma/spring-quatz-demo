package com.gcs.quartz.registry;

import com.gcs.quartz.model.JobDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Registry for managing all Quartz job definitions
 * This allows easy registration and retrieval of jobs
 */
@Component
public class JobRegistry {

    private static final Logger logger = LoggerFactory.getLogger(JobRegistry.class);
    private final Map<String, JobDefinition> jobs = new LinkedHashMap<>();

    /**
     * Register a job definition
     */
    public void registerJob(JobDefinition jobDefinition) {
        String key = jobDefinition.getJobGroup() + ":" + jobDefinition.getJobName();
        jobs.put(key, jobDefinition);
        logger.info("Registered job: {} in group: {}", jobDefinition.getJobName(), jobDefinition.getJobGroup());
    }

    /**
     * Get a specific job definition
     */
    public JobDefinition getJob(String jobGroup, String jobName) {
        return jobs.get(jobGroup + ":" + jobName);
    }

    /**
     * Get all registered jobs
     */
    public Collection<JobDefinition> getAllJobs() {
        return jobs.values();
    }

    /**
     * Get all enabled jobs
     */
    public Collection<JobDefinition> getEnabledJobs() {
        List<JobDefinition> enabledJobs = new ArrayList<>();
        for (JobDefinition job : jobs.values()) {
            if (job.isEnabled()) {
                enabledJobs.add(job);
            }
        }
        return enabledJobs;
    }

    /**
     * Get jobs by group
     */
    public Collection<JobDefinition> getJobsByGroup(String jobGroup) {
        List<JobDefinition> groupJobs = new ArrayList<>();
        for (JobDefinition job : jobs.values()) {
            if (job.getJobGroup().equals(jobGroup)) {
                groupJobs.add(job);
            }
        }
        return groupJobs;
    }

    /**
     * Check if a job is registered
     */
    public boolean jobExists(String jobGroup, String jobName) {
        return jobs.containsKey(jobGroup + ":" + jobName);
    }

    /**
     * Get total number of registered jobs
     */
    public int getTotalJobs() {
        return jobs.size();
    }

    /**
     * Update an existing job definition
     */
    public void updateJob(JobDefinition jobDefinition) {
        String key = jobDefinition.getJobGroup() + ":" + jobDefinition.getJobName();
        if (jobs.containsKey(key)) {
            jobs.put(key, jobDefinition);
            logger.info("Updated job definition in registry: {} in group: {}",
                    jobDefinition.getJobName(), jobDefinition.getJobGroup());
        } else {
            logger.warn("Attempted to update non-existent job: {} in group: {}",
                    jobDefinition.getJobName(), jobDefinition.getJobGroup());
        }
    }
}

