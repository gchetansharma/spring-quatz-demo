package com.gcs.quartz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating a job
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobUpdateRequestDTO {

    private String cronExpression;
    private String description;
    private Boolean enabled;
}

