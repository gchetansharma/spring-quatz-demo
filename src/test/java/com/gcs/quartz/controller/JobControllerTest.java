package com.gcs.quartz.controller;

import com.gcs.quartz.dto.JobInfoDTO;
import com.gcs.quartz.dto.JobUpdateRequestDTO;
import com.gcs.quartz.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.SchedulerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobControllerTest {

    @Mock
    private JobService jobService;

    private JobController jobController;

    @BeforeEach
    void setUp() {
        jobController = new JobController(jobService);
    }

    @Test
    void testGetAllJobs() {
        // Arrange
        JobInfoDTO jobDTO = JobInfoDTO.builder()
            .jobName("TestJob")
            .jobGroup("TestGroup")
            .triggerName("TestTrigger")
            .triggerGroup("TestGroup")
            .cronExpression("0 0 * * * ?")
            .description("Test Description")
            .jobClassName("com.gcs.quartz.job.TestJob")
            .enabled(true)
            .build();

        when(jobService.getAllJobs()).thenReturn(Arrays.asList(jobDTO));

        // Act
        List<JobInfoDTO> result = jobController.getAllJobs();

        // Assert
        assertEquals(1, result.size());
        assertEquals("TestJob", result.get(0).getJobName());
        verify(jobService, times(1)).getAllJobs();
    }

    @Test
    void testGetJob_Found() {
        // Arrange
        JobInfoDTO jobDTO = JobInfoDTO.builder()
            .jobName("TestJob")
            .jobGroup("TestGroup")
            .triggerName("TestTrigger")
            .triggerGroup("TestGroup")
            .cronExpression("0 0 * * * ?")
            .description("Test Description")
            .jobClassName("com.gcs.quartz.job.TestJob")
            .enabled(true)
            .build();

        when(jobService.getJob("TestGroup", "TestJob")).thenReturn(Optional.of(jobDTO));

        // Act
        ResponseEntity<JobInfoDTO> result = jobController.getJob("TestGroup", "TestJob");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("TestJob", result.getBody().getJobName());
        verify(jobService, times(1)).getJob("TestGroup", "TestJob");
    }

    @Test
    void testGetJob_NotFound() {
        // Arrange
        when(jobService.getJob("NonExistent", "NonExistent")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<JobInfoDTO> result = jobController.getJob("NonExistent", "NonExistent");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(jobService, times(1)).getJob("NonExistent", "NonExistent");
    }

    @Test
    void testUpdateJob_Success() throws SchedulerException {
        // Arrange
        JobUpdateRequestDTO updateRequest = new JobUpdateRequestDTO();
        updateRequest.setCronExpression("0 */15 * * * ?");

        when(jobService.updateJob("TestGroup", "TestJob", updateRequest)).thenReturn(true);

        // Act
        ResponseEntity<?> result = jobController.updateJob("TestGroup", "TestJob", updateRequest);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(jobService, times(1)).updateJob("TestGroup", "TestJob", updateRequest);
    }

    @Test
    void testUpdateJob_NotFound() throws SchedulerException {
        // Arrange
        JobUpdateRequestDTO updateRequest = new JobUpdateRequestDTO();
        updateRequest.setCronExpression("0 */15 * * * ?");

        when(jobService.updateJob("NonExistent", "NonExistent", updateRequest)).thenReturn(false);

        // Act
        ResponseEntity<?> result = jobController.updateJob("NonExistent", "NonExistent", updateRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void testStartJob_Success() throws SchedulerException {
        // Arrange
        when(jobService.startJob("TestGroup", "TestJob")).thenReturn(true);

        // Act
        ResponseEntity<?> result = jobController.startJob("TestGroup", "TestJob");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().toString().contains("triggered successfully"));
        verify(jobService, times(1)).startJob("TestGroup", "TestJob");
    }

    @Test
    void testStartJob_NotFound() throws SchedulerException {
        // Arrange
        when(jobService.startJob("NonExistent", "NonExistent")).thenReturn(false);

        // Act
        ResponseEntity<?> result = jobController.startJob("NonExistent", "NonExistent");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(jobService, times(1)).startJob("NonExistent", "NonExistent");
    }

    @Test
    void testStartJob_SchedulerException() throws SchedulerException {
        // Arrange
        when(jobService.startJob("TestGroup", "TestJob"))
            .thenThrow(new SchedulerException("Scheduler error"));

        // Act
        ResponseEntity<?> result = jobController.startJob("TestGroup", "TestJob");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertTrue(result.getBody().toString().contains("Error starting job"));
    }

    @Test
    void testPauseJob_Success() throws SchedulerException {
        // Arrange
        when(jobService.pauseJob("TestGroup", "TestJob")).thenReturn(true);

        // Act
        ResponseEntity<?> result = jobController.pauseJob("TestGroup", "TestJob");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().toString().contains("paused successfully"));
        verify(jobService, times(1)).pauseJob("TestGroup", "TestJob");
    }

    @Test
    void testPauseJob_NotFound() throws SchedulerException {
        // Arrange
        when(jobService.pauseJob("NonExistent", "NonExistent")).thenReturn(false);

        // Act
        ResponseEntity<?> result = jobController.pauseJob("NonExistent", "NonExistent");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void testResumeJob_Success() throws SchedulerException {
        // Arrange
        when(jobService.resumeJob("TestGroup", "TestJob")).thenReturn(true);

        // Act
        ResponseEntity<?> result = jobController.resumeJob("TestGroup", "TestJob");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().toString().contains("resumed successfully"));
        verify(jobService, times(1)).resumeJob("TestGroup", "TestJob");
    }

    @Test
    void testResumeJob_NotFound() throws SchedulerException {
        // Arrange
        when(jobService.resumeJob("NonExistent", "NonExistent")).thenReturn(false);

        // Act
        ResponseEntity<?> result = jobController.resumeJob("NonExistent", "NonExistent");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void testStopJob_Success() throws SchedulerException {
        // Arrange
        when(jobService.stopJob("TestGroup", "TestJob")).thenReturn(true);

        // Act
        ResponseEntity<?> result = jobController.stopJob("TestGroup", "TestJob");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().toString().contains("stopped and deleted successfully"));
        verify(jobService, times(1)).stopJob("TestGroup", "TestJob");
    }

    @Test
    void testStopJob_NotFound() throws SchedulerException {
        // Arrange
        when(jobService.stopJob("NonExistent", "NonExistent")).thenReturn(false);

        // Act
        ResponseEntity<?> result = jobController.stopJob("NonExistent", "NonExistent");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void testStopJob_SchedulerException() throws SchedulerException {
        // Arrange
        when(jobService.stopJob("TestGroup", "TestJob"))
            .thenThrow(new SchedulerException("Scheduler error"));

        // Act
        ResponseEntity<?> result = jobController.stopJob("TestGroup", "TestJob");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertTrue(result.getBody().toString().contains("Error stopping job"));
    }
}

