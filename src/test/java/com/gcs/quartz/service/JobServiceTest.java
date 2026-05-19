package com.gcs.quartz.service;

import com.gcs.quartz.dto.JobInfoDTO;
import com.gcs.quartz.dto.JobUpdateRequestDTO;
import com.gcs.quartz.job.LoggingJob;
import com.gcs.quartz.model.JobDefinition;
import com.gcs.quartz.registry.JobRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRegistry jobRegistry;

    @Mock
    private Scheduler scheduler;

    private JobService jobService;

    @BeforeEach
    void setUp() {
        jobService = new JobService(jobRegistry, scheduler);
    }

    @Test
    void testGetAllJobs() {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "TestTrigger", "TestGroup",
            "0 0 * * * ?", "Test Description", LoggingJob.class, true
        );
        when(jobRegistry.getAllJobs()).thenReturn(Arrays.asList(jobDef));

        // Act
        List<JobInfoDTO> result = jobService.getAllJobs();

        // Assert
        assertEquals(1, result.size());
        assertEquals("TestJob", result.get(0).getJobName());
        assertEquals("TestGroup", result.get(0).getJobGroup());
        verify(jobRegistry, times(1)).getAllJobs();
    }

    @Test
    void testGetJob_Found() {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "TestTrigger", "TestGroup",
            "0 0 * * * ?", "Test Description", LoggingJob.class, true
        );
        when(jobRegistry.getJob("TestGroup", "TestJob")).thenReturn(jobDef);

        // Act
        Optional<JobInfoDTO> result = jobService.getJob("TestGroup", "TestJob");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("TestJob", result.get().getJobName());
        verify(jobRegistry, times(1)).getJob("TestGroup", "TestJob");
    }

    @Test
    void testGetJob_NotFound() {
        // Arrange
        when(jobRegistry.getJob("NonExistent", "NonExistent")).thenReturn(null);

        // Act
        Optional<JobInfoDTO> result = jobService.getJob("NonExistent", "NonExistent");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testStartJob_Success() throws SchedulerException {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "TestTrigger", "TestGroup",
            "0 0 * * * ?", "Test Description", LoggingJob.class, true
        );
        when(jobRegistry.getJob("TestGroup", "TestJob")).thenReturn(jobDef);
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);

        // Act
        boolean result = jobService.startJob("TestGroup", "TestJob");

        // Assert
        assertTrue(result);
        verify(scheduler, times(1)).triggerJob(any(JobKey.class));
    }

    @Test
    void testStartJob_JobNotFound() throws SchedulerException {
        // Arrange
        when(jobRegistry.getJob("NonExistent", "NonExistent")).thenReturn(null);

        // Act
        boolean result = jobService.startJob("NonExistent", "NonExistent");

        // Assert
        assertFalse(result);
        verify(scheduler, never()).triggerJob(any(JobKey.class));
    }

    @Test
    void testStartJob_NotScheduled() throws SchedulerException {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "TestTrigger", "TestGroup",
            "0 0 * * * ?", "Test Description", LoggingJob.class, true
        );
        when(jobRegistry.getJob("TestGroup", "TestJob")).thenReturn(jobDef);
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);

        // Act
        boolean result = jobService.startJob("TestGroup", "TestJob");

        // Assert
        assertFalse(result);
        verify(scheduler, never()).triggerJob(any(JobKey.class));
    }

    @Test
    void testPauseJob_Success() throws SchedulerException {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "TestTrigger", "TestGroup",
            "0 0 * * * ?", "Test Description", LoggingJob.class, true
        );
        when(jobRegistry.getJob("TestGroup", "TestJob")).thenReturn(jobDef);
        when(scheduler.checkExists(any(TriggerKey.class))).thenReturn(true);

        // Act
        boolean result = jobService.pauseJob("TestGroup", "TestJob");

        // Assert
        assertTrue(result);
        verify(scheduler, times(1)).pauseTrigger(any(TriggerKey.class));
    }

    @Test
    void testPauseJob_JobNotFound() throws SchedulerException {
        // Arrange
        when(jobRegistry.getJob("NonExistent", "NonExistent")).thenReturn(null);

        // Act
        boolean result = jobService.pauseJob("NonExistent", "NonExistent");

        // Assert
        assertFalse(result);
        verify(scheduler, never()).pauseTrigger(any(TriggerKey.class));
    }

    @Test
    void testResumeJob_Success() throws SchedulerException {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "TestTrigger", "TestGroup",
            "0 0 * * * ?", "Test Description", LoggingJob.class, true
        );
        when(jobRegistry.getJob("TestGroup", "TestJob")).thenReturn(jobDef);
        when(scheduler.checkExists(any(TriggerKey.class))).thenReturn(true);

        // Act
        boolean result = jobService.resumeJob("TestGroup", "TestJob");

        // Assert
        assertTrue(result);
        verify(scheduler, times(1)).resumeTrigger(any(TriggerKey.class));
    }

    @Test
    void testResumeJob_JobNotFound() throws SchedulerException {
        // Arrange
        when(jobRegistry.getJob("NonExistent", "NonExistent")).thenReturn(null);

        // Act
        boolean result = jobService.resumeJob("NonExistent", "NonExistent");

        // Assert
        assertFalse(result);
        verify(scheduler, never()).resumeTrigger(any(TriggerKey.class));
    }

    @Test
    void testStopJob_Success() throws SchedulerException {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "TestTrigger", "TestGroup",
            "0 0 * * * ?", "Test Description", LoggingJob.class, true
        );
        when(jobRegistry.getJob("TestGroup", "TestJob")).thenReturn(jobDef);
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);

        // Act
        boolean result = jobService.stopJob("TestGroup", "TestJob");

        // Assert
        assertTrue(result);
        verify(scheduler, times(1)).deleteJob(any(JobKey.class));
    }

    @Test
    void testStopJob_JobNotFound() throws SchedulerException {
        // Arrange
        when(jobRegistry.getJob("NonExistent", "NonExistent")).thenReturn(null);

        // Act
        boolean result = jobService.stopJob("NonExistent", "NonExistent");

        // Assert
        assertFalse(result);
        verify(scheduler, never()).deleteJob(any(JobKey.class));
    }

    @Test
    void testStopJob_NotScheduled() throws SchedulerException {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "TestTrigger", "TestGroup",
            "0 0 * * * ?", "Test Description", LoggingJob.class, true
        );
        when(jobRegistry.getJob("TestGroup", "TestJob")).thenReturn(jobDef);
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(false);

        // Act
        boolean result = jobService.stopJob("TestGroup", "TestJob");

        // Assert
        assertFalse(result);
        verify(scheduler, never()).deleteJob(any(JobKey.class));
    }

    @Test
    void testUpdateJob_UpdatesCronExpression() throws SchedulerException {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "TestTrigger", "TestGroup",
            "0 0 * * * ?", "Test Description", LoggingJob.class, true
        );
        when(jobRegistry.getJob("TestGroup", "TestJob")).thenReturn(jobDef);
        when(scheduler.checkExists(any(JobKey.class))).thenReturn(true);
        when(scheduler.getTrigger(any(TriggerKey.class))).thenReturn(mock(Trigger.class));

        JobUpdateRequestDTO updateRequest = new JobUpdateRequestDTO();
        updateRequest.setCronExpression("0 */15 * * * ?");

        // Act
        boolean result = jobService.updateJob("TestGroup", "TestJob", updateRequest);

        // Assert
        assertTrue(result);
        assertEquals("0 */15 * * * ?", jobDef.getCronExpression());
        verify(jobRegistry, times(1)).updateJob(jobDef);
    }

    @Test
    void testUpdateJob_JobNotFound() throws SchedulerException {
        // Arrange
        when(jobRegistry.getJob("NonExistent", "NonExistent")).thenReturn(null);

        JobUpdateRequestDTO updateRequest = new JobUpdateRequestDTO();
        updateRequest.setCronExpression("0 */15 * * * ?");

        // Act
        boolean result = jobService.updateJob("NonExistent", "NonExistent", updateRequest);

        // Assert
        assertFalse(result);
    }
}

