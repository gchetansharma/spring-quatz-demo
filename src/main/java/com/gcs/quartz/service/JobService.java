package com.gcs.quartz.service;

import com.gcs.quartz.dto.JobInfoDTO;
import com.gcs.quartz.dto.JobUpdateRequestDTO;
import com.gcs.quartz.model.JobDefinition;
import com.gcs.quartz.registry.JobRegistry;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private final JobRegistry jobRegistry;
    private final Scheduler scheduler;

    public JobService(JobRegistry jobRegistry, Scheduler scheduler) {
        this.jobRegistry = jobRegistry;
        this.scheduler = scheduler;
    }

    public List<JobInfoDTO> getAllJobs() {
        return jobRegistry.getAllJobs().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<JobInfoDTO> getJob(String group, String name) {
        JobDefinition job = jobRegistry.getJob(group, name);
        return Optional.ofNullable(job).map(this::convertToDTO);
    }

    public boolean updateJob(String group, String name, JobUpdateRequestDTO request) throws SchedulerException {
        JobDefinition jobDefinition = jobRegistry.getJob(group, name);
        if (jobDefinition == null) {
            return false;
        }

        boolean shouldReschedule = false;
        boolean statusChanged = false;

        if (request.getCronExpression() != null && !request.getCronExpression().equals(jobDefinition.getCronExpression())) {
            jobDefinition.setCronExpression(request.getCronExpression());
            shouldReschedule = true;
        }

        if (request.getDescription() != null) {
            jobDefinition.setDescription(request.getDescription());
        }

        if (request.getEnabled() != null && request.getEnabled() != jobDefinition.isEnabled()) {
            jobDefinition.setEnabled(request.getEnabled());
            statusChanged = true;
        }

        jobRegistry.updateJob(jobDefinition);

        if (jobDefinition.isEnabled()) {
            updateQuartzJob(jobDefinition, shouldReschedule || statusChanged);
        } else if (statusChanged) {
            // If it was just disabled
            scheduler.deleteJob(new JobKey(name, group));
            logger.info("Deleted job from scheduler as it was disabled: {}/{}", group, name);
        }

        return true;
    }

    private void updateQuartzJob(JobDefinition jobDefinition, boolean forceUpdate) throws SchedulerException {
        JobKey jobKey = new JobKey(jobDefinition.getJobName(), jobDefinition.getJobGroup());
        TriggerKey triggerKey = new TriggerKey(jobDefinition.getTriggerName(), jobDefinition.getTriggerGroup());

        if (scheduler.checkExists(jobKey)) {
            if (forceUpdate) {
                Trigger oldTrigger = scheduler.getTrigger(triggerKey);
                if (oldTrigger != null) {
                    Trigger newTrigger = TriggerBuilder.newTrigger()
                            .withIdentity(triggerKey)
                            .withDescription(jobDefinition.getDescription())
                            .withSchedule(CronScheduleBuilder.cronSchedule(jobDefinition.getCronExpression()))
                            .build();
                    scheduler.rescheduleJob(triggerKey, newTrigger);
                    logger.info("Rescheduled job: {}/{}", jobDefinition.getJobGroup(), jobDefinition.getJobName());
                } else {
                    // Trigger doesn't exist but job does? Re-register trigger.
                    registerJobInScheduler(jobDefinition);
                }
            }
        } else {
            // Job doesn't exist in scheduler, register it
            registerJobInScheduler(jobDefinition);
        }
    }

    private void registerJobInScheduler(JobDefinition jobDefinition) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(jobDefinition.getJobClass())
                .withIdentity(jobDefinition.getJobName(), jobDefinition.getJobGroup())
                .withDescription(jobDefinition.getDescription())
                .storeDurably()
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDefinition.getTriggerName(), jobDefinition.getTriggerGroup())
                .withDescription(jobDefinition.getDescription())
                .withSchedule(CronScheduleBuilder.cronSchedule(jobDefinition.getCronExpression()))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        logger.info("Scheduled new job: {}/{}", jobDefinition.getJobGroup(), jobDefinition.getJobName());
    }

    /**
     * Manually trigger a job to run immediately
     */
    public boolean startJob(String group, String name) throws SchedulerException {
        JobDefinition jobDefinition = jobRegistry.getJob(group, name);
        if (jobDefinition == null) {
            logger.warn("Job not found: {}/{}", group, name);
            return false;
        }

        JobKey jobKey = new JobKey(name, group);
        if (!scheduler.checkExists(jobKey)) {
            logger.warn("Job not scheduled with Quartz: {}/{}", group, name);
            return false;
        }

        scheduler.triggerJob(jobKey);
        logger.info("Manually triggered job: {}/{}", group, name);
        return true;
    }

    /**
     * Pause a job's trigger (stops scheduled execution, job remains registered)
     */
    public boolean pauseJob(String group, String name) throws SchedulerException {
        JobDefinition jobDefinition = jobRegistry.getJob(group, name);
        if (jobDefinition == null) {
            logger.warn("Job not found: {}/{}", group, name);
            return false;
        }

        TriggerKey triggerKey = new TriggerKey(jobDefinition.getTriggerName(), jobDefinition.getTriggerGroup());
        if (!scheduler.checkExists(triggerKey)) {
            logger.warn("Trigger not found: {}/{}", jobDefinition.getTriggerGroup(), jobDefinition.getTriggerName());
            return false;
        }

        scheduler.pauseTrigger(triggerKey);
        logger.info("Paused job trigger: {}/{}", group, name);
        return true;
    }

    /**
     * Resume a paused job's trigger
     */
    public boolean resumeJob(String group, String name) throws SchedulerException {
        JobDefinition jobDefinition = jobRegistry.getJob(group, name);
        if (jobDefinition == null) {
            logger.warn("Job not found: {}/{}", group, name);
            return false;
        }

        TriggerKey triggerKey = new TriggerKey(jobDefinition.getTriggerName(), jobDefinition.getTriggerGroup());
        if (!scheduler.checkExists(triggerKey)) {
            logger.warn("Trigger not found: {}/{}", jobDefinition.getTriggerGroup(), jobDefinition.getTriggerName());
            return false;
        }

        scheduler.resumeTrigger(triggerKey);
        logger.info("Resumed job trigger: {}/{}", group, name);
        return true;
    }

    /**
     * Stop a job (delete from scheduler)
     */
    public boolean stopJob(String group, String name) throws SchedulerException {
        JobDefinition jobDefinition = jobRegistry.getJob(group, name);
        if (jobDefinition == null) {
            logger.warn("Job not found: {}/{}", group, name);
            return false;
        }

        JobKey jobKey = new JobKey(name, group);
        if (!scheduler.checkExists(jobKey)) {
            logger.warn("Job not scheduled with Quartz: {}/{}", group, name);
            return false;
        }

        scheduler.deleteJob(jobKey);
        logger.info("Stopped and deleted job: {}/{}", group, name);
        return true;
    }

    private JobInfoDTO convertToDTO(JobDefinition job) {
        return JobInfoDTO.builder()
                .jobName(job.getJobName())
                .jobGroup(job.getJobGroup())
                .triggerName(job.getTriggerName())
                .triggerGroup(job.getTriggerGroup())
                .cronExpression(job.getCronExpression())
                .description(job.getDescription())
                .jobClassName(job.getJobClass().getName())
                .enabled(job.isEnabled())
                .build();
    }
}
