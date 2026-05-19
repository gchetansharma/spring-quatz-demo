# Extending the Quartz Jobs Framework

This document explains how to add new jobs and job groups to the application in a scalable and maintainable way.

## Architecture Overview

The application uses a registry-based architecture for managing Quartz jobs:

1. **JobRegistry**: Central registry for all job definitions
2. **JobDefinition**: Metadata container for jobs (name, group, cron expression, etc.)
3. **JobRegistrationConfig**: Configuration class where jobs are registered
4. **JobTriggerFactory**: Factory for creating JobDetail and Trigger instances
5. **QuartzJobBase**: Base interface for all jobs

## Adding a New Job

Follow these steps to add a new job to the application:

### Step 1: Create the Job Class

Create a new job class that implements `QuartzJobBase`:

```java
package com.gcs.quartz.job;

import com.gcs.quartz.config.EmailJobProperties;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationJob implements QuartzJobBase {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationJob.class);
    private static final String JOB_NAME = "EmailNotificationJob";
    private static final String JOB_GROUP = "EmailGroup";
    private static final String CRON_EXPRESSION = "0 0 9 * * ?"; // Every day at 9 AM
    
    private EmailJobProperties emailJobProperties;
    
    public EmailNotificationJob(EmailJobProperties emailJobProperties) {
        this.emailJobProperties = emailJobProperties;
    }
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("Executing EmailNotificationJob");
        // Implement job logic here
    }
    
    @Override
    public String getJobName() {
        return JOB_NAME;
    }
    
    @Override
    public String getJobGroup() {
        return JOB_GROUP;
    }
    
    @Override
    public String getCronExpression() {
        return CRON_EXPRESSION;
    }
}
```

### Step 2: Create Properties Class (Optional)

If your job needs externalized configuration properties:

```java
package com.gcs.quartz.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "job.email")
@Getter
@Setter
public class EmailJobProperties {
    
    private String sender;
    private String recipient;
    private String subject;
    private String body;
    // Add more properties as needed
}
```

### Step 3: Add Properties to Configuration Files

Add the job properties to the application properties files:

**application.properties:**
```properties
job.email.sender=noreply@example.com
job.email.recipient=admin@example.com
job.email.subject=Daily Report
job.email.body=Please find the daily report attached.
```

**application-dev.properties:**
```properties
job.email.sender=dev-noreply@example.com
job.email.recipient=dev-admin@example.com
```

**application-staging.properties:**
```properties
job.email.sender=staging-noreply@example.com
job.email.recipient=staging-admin@example.com
```

**application-prod.properties:**
```properties
job.email.sender=prod-noreply@example.com
job.email.recipient=prod-admin@example.com
```

### Step 4: Register the Job in JobRegistrationConfig

Edit `src/main/java/com/gcs/quartz/config/JobRegistrationConfig.java`:

```java
@PostConstruct
public void registerAllJobs() {
    registerLoggingJob();
    registerEmailNotificationJob();  // Add this line
}

private void registerEmailNotificationJob() {
    JobDefinition emailJobDef = new JobDefinition(
            "EmailNotificationJob",                    // Job Name
            "EmailGroup",                              // Job Group
            "EmailTrigger",                            // Trigger Name
            "EmailGroup",                              // Trigger Group
            "0 0 9 * * ?",                             // Cron Expression (Daily at 9 AM)
            "Job that sends email notifications",     // Description
            EmailNotificationJob.class,                // Job Class
            true                                       // Enabled
    );
    
    jobRegistry.registerJob(emailJobDef);
}
```

## Job Cron Expression Examples

| Schedule | Cron Expression |
|----------|-----------------|
| Every 30 minutes on weekdays | `0 0/30 * ? * MON-FRI` |
| Every day at 9:00 AM | `0 0 9 * * ?` |
| Every 15 minutes | `0 */15 * * * ?` |
| Every hour | `0 0 * * * ?` |
| Every Monday at 9:00 AM | `0 0 9 ? * MON` |
| Every 1st of the month at midnight | `0 0 0 1 * ?` |
| Every 5 seconds | `*/5 * * * * ?` |

## Creating New Job Groups

To organize jobs into different groups, simply use a different `jobGroup` value in the `JobDefinition`:

```java
// EmailGroup for email-related jobs
private void registerEmailNotificationJob() {
    JobDefinition emailJobDef = new JobDefinition(
            "EmailNotificationJob",
            "EmailGroup",  // New group
            "EmailTrigger",
            "EmailGroup",
            "0 0 9 * * ?",
            "Job that sends email notifications",
            EmailNotificationJob.class,
            true
    );
    jobRegistry.registerJob(emailJobDef);
}

// SyncGroup for data synchronization jobs
private void registerDataSyncJob() {
    JobDefinition syncJobDef = new JobDefinition(
            "DataSyncJob",
            "SyncGroup",  // Another group
            "SyncTrigger",
            "SyncGroup",
            "0 */15 * * * ?",
            "Job that synchronizes data",
            DataSyncJob.class,
            true
    );
    jobRegistry.registerJob(syncJobDef);
}
```

## Disabling a Job

To disable a job without removing it from the registry, pass `false` in the enabled flag:

```java
JobDefinition jobDef = new JobDefinition(
    "JobName",
    "GroupName",
    "TriggerName",
    "GroupName",
    "0 0 * * * ?",
    "Description",
    JobClass.class,
    false  // Disabled
);
jobRegistry.registerJob(jobDef);
```

## Key Components

### JobRegistry
Located in: `src/main/java/com/gcs/quartz/registry/JobRegistry.java`

Methods:
- `registerJob(JobDefinition jobDefinition)` - Register a job
- `getJob(String jobGroup, String jobName)` - Get a specific job
- `updateJob(JobDefinition jobDefinition)` - Update an existing job ⭐
- `getAllJobs()` - Get all registered jobs
- `getEnabledJobs()` - Get only enabled jobs
- `getJobsByGroup(String jobGroup)` - Get jobs by group
- `jobExists(String jobGroup, String jobName)` - Check if job exists

### JobDefinition
Located in: `src/main/java/com/gcs/quartz/model/JobDefinition.java`

Constructor parameters:
- `jobName` - Name of the job
- `jobGroup` - Group name for organizing jobs
- `triggerName` - Name of the trigger
- `triggerGroup` - Group name for the trigger
- `cronExpression` - Cron expression for scheduling
- `description` - Job description
- `jobClass` - The job implementation class
- `enabled` - Whether the job should run

### QuartzJobBase
Located in: `src/main/java/com/gcs/quartz/job/QuartzJobBase.java`

Methods to implement:
- `execute(JobExecutionContext context)` - Job execution logic (from Quartz Job interface)
- `getJobName()` - Return job name constant
- `getJobGroup()` - Return job group constant
- `getCronExpression()` - Return cron expression constant
- `isEnabled()` - Optional: return whether job is enabled (defaults to true)

## Best Practices

1. **Use Constants**: Store job name, group, and cron expression as static constants in your job class
2. **Separate Properties**: Create a dedicated properties class for each job group
3. **Environment-Specific**: Override properties in environment-specific files (dev, staging, prod)
4. **Clear Naming**: Use descriptive names for jobs and groups
5. **Documentation**: Document the job's purpose and schedule
6. **Error Handling**: Implement proper exception handling in job execution
7. **Logging**: Log job execution start, success, and failures
8. **Idempotent**: Design jobs to be safe to run multiple times

## File Reference

```
src/main/java/com/gcs/quartz/
├── job/
│   ├── QuartzJobBase.java          (Base interface)
│   ├── LoggingJob.java             (Example job)
│   └── [YourJobName].java          (Add new jobs here)
├── controller/
│   └── JobController.java          (REST API & Swagger UI ⭐)
├── service/
│   └── JobService.java             (Job Management Service ⭐)
├── dto/
│   ├── JobInfoDTO.java             (Job Data DTO)
│   └── JobUpdateRequestDTO.java    (Update Request DTO)
├── config/
│   ├── QuartzConfig.java           (Quartz configuration)
│   ├── JobRegistrationConfig.java  (Job registration - EDIT THIS)
│   ├── LoggingJobProperties.java   (Job properties)
│   └── [YourJobProperties].java    (Add new property classes here)
├── registry/
│   └── JobRegistry.java            (Job registry - DO NOT EDIT)
├── factory/
│   ├── JobTriggerFactory.java      (Job/trigger factory - DO NOT EDIT)
│   └── QuartzJobFactory.java       (Spring integration - DO NOT EDIT)
└── model/
    └── JobDefinition.java          (Job definition model - DO NOT EDIT)

src/main/resources/
├── application.properties           (Base properties - add job properties here)
├── application-dev.properties       (Dev overrides)
├── application-staging.properties   (Staging overrides)
└── application-prod.properties      (Prod overrides)
```

## Troubleshooting

### Job not running
1. Check if job is registered in `JobRegistrationConfig`
2. Verify the cron expression is correct using a cron validator
3. Check if job is enabled (last parameter in JobDefinition)
4. Review application logs for errors

### Properties not loading
1. Verify properties class uses correct prefix
2. Ensure @Component and @ConfigurationProperties annotations are present
3. Check property names use kebab-case (e.g., `job.email.sender`)
4. Verify properties are defined in the appropriate application-*.properties file

### Job instances not found
1. Ensure job class implements QuartzJobBase
2. Verify job class has @Component annotation
3. Check job class has required constructor with properties dependency
4. Review Spring Boot component scan configuration

