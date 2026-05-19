package com.gcs.quartz.model;

import org.quartz.Job;

/**
 * Definition of a Quartz job with metadata
 */
public class JobDefinition {

    private String jobName;
    private String jobGroup;
    private String triggerName;
    private String triggerGroup;
    private String cronExpression;
    private String description;
    private Class<? extends Job> jobClass;
    private boolean enabled;

    public JobDefinition(String jobName, String jobGroup, String triggerName, String triggerGroup,
                        String cronExpression, String description, Class<? extends Job> jobClass,
                        boolean enabled) {
        this.jobName = jobName;
        this.jobGroup = jobGroup;
        this.triggerName = triggerName;
        this.triggerGroup = triggerGroup;
        this.cronExpression = cronExpression;
        this.description = description;
        this.jobClass = jobClass;
        this.enabled = enabled;
    }

    // Getters and Setters
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public String getTriggerGroup() {
        return triggerGroup;
    }

    public void setTriggerGroup(String triggerGroup) {
        this.triggerGroup = triggerGroup;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Class<? extends Job> getJobClass() {
        return jobClass;
    }

    public void setJobClass(Class<? extends Job> jobClass) {
        this.jobClass = jobClass;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

