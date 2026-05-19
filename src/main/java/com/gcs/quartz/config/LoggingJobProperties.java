package com.gcs.quartz.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the LoggingJob
 * Loaded from application properties with prefix "job.logging"
 */
@Component
@ConfigurationProperties(prefix = "job.logging")
@Getter
@Setter
public class LoggingJobProperties {

    private String logSeparator;
    private String jobTitle;
    private String executionTimeLabel;
    private String jobNameLabel;
    private String jobGroupLabel;
    private String triggerNameLabel;
    private String nextFireTimeLabel;
    private String dateTimeFormat;
}


