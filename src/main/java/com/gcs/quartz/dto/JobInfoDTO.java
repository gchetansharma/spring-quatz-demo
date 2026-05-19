package com.gcs.quartz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object for Job information
 */
@Data
@Builder
@AllArgsConstructor
public class JobInfoDTO {

    private String jobName;
    private String jobGroup;
    private String triggerName;
    private String triggerGroup;
    private String cronExpression;
    private String description;
    private String jobClassName;
    private boolean enabled;
}

