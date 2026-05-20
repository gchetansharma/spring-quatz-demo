package com.gcs.quartz.job;

import com.gcs.quartz.config.LoggingJobProperties;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LoggingJob implements QuartzJobBase {
    private static final Logger logger = LoggerFactory.getLogger(LoggingJob.class);
    private static final String JOB_NAME = "LoggingJob";
    private static final String JOB_GROUP = "WeekdayGroup";
    private static final String CRON_EXPRESSION = "0 0/30 * ? * MON-FRI";

    private LoggingJobProperties loggingJobProperties;

    public LoggingJob(LoggingJobProperties loggingJobProperties) {
        this.loggingJobProperties = loggingJobProperties;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(loggingJobProperties.getDateTimeFormat());
        String formattedTime = now.format(formatter);

        logger.info(loggingJobProperties.getLogSeparator());
        logger.info(loggingJobProperties.getJobTitle());
        logger.info("{} {}", loggingJobProperties.getExecutionTimeLabel(), formattedTime);
        logger.info("{} {}", loggingJobProperties.getJobNameLabel(), context.getJobDetail().getKey().getName());
        logger.info("{} {}", loggingJobProperties.getJobGroupLabel(), context.getJobDetail().getKey().getGroup());
        logger.info("{} {}", loggingJobProperties.getTriggerNameLabel(), context.getTrigger().getKey().getName());
        logger.info("{} {}", loggingJobProperties.getNextFireTimeLabel(), context.getNextFireTime());
        logger.info(loggingJobProperties.getLogSeparator());
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

