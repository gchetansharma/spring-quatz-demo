package com.gcs.quartz.factory;

import com.gcs.quartz.job.LoggingJob;
import com.gcs.quartz.model.JobDefinition;
import com.gcs.quartz.registry.JobRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JobTriggerFactoryTest {

    @Mock
    private JobRegistry jobRegistry;

    private JobTriggerFactory jobTriggerFactory;

    @BeforeEach
    void setUp() {
        jobTriggerFactory = new JobTriggerFactory(jobRegistry);
    }

    @Test
    void testCreateJobDetail() {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "TestTrigger", "TestGroup",
            "0 0 * * * ?", "Test Description", LoggingJob.class, true
        );

        // Act
        JobDetail jobDetail = jobTriggerFactory.createJobDetail(jobDef);

        // Assert
        assertNotNull(jobDetail);
        assertEquals("TestJob", jobDetail.getKey().getName());
        assertEquals("TestGroup", jobDetail.getKey().getGroup());
        assertEquals(LoggingJob.class, jobDetail.getJobClass());
        assertEquals("Test Description", jobDetail.getDescription());
    }

    @Test
    void testCreateTrigger() {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "TestTrigger", "TestGroup",
            "0 0 * * * ?", "Test Description", LoggingJob.class, true
        );
        JobDetail jobDetail = jobTriggerFactory.createJobDetail(jobDef);

        // Act
        Trigger trigger = jobTriggerFactory.createTrigger(jobDef, jobDetail);

        // Assert
        assertNotNull(trigger);
        assertEquals("TestTrigger", trigger.getKey().getName());
        assertEquals("TestGroup", trigger.getKey().getGroup());
        assertEquals("Test Description", trigger.getDescription());
        assertEquals(jobDetail.getKey(), trigger.getJobKey());
    }

    @Test
    void testCreateTriggerWithValidCronExpression() {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "TestTrigger", "TestGroup",
            "0 0/30 * ? * MON-FRI", "Every 30 mins on weekdays", LoggingJob.class, true
        );
        JobDetail jobDetail = jobTriggerFactory.createJobDetail(jobDef);

        // Act
        Trigger trigger = jobTriggerFactory.createTrigger(jobDef, jobDetail);

        // Assert
        assertNotNull(trigger);
        assertNotNull(trigger.getStartTime());
        assertNotNull(trigger.getKey());
    }

    @Test
    void testCreateJobDetailWithDurableFlag() {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "PersistentJob", "PersistentGroup", "PersistentTrigger", "PersistentGroup",
            "0 0 * * * ?", "Persistent Job Description", LoggingJob.class, true
        );

        // Act
        JobDetail jobDetail = jobTriggerFactory.createJobDetail(jobDef);

        // Assert
        assertNotNull(jobDetail);
        assertTrue(jobDetail.isDurable());
    }

    @Test
    void testTriggerFiresBeforeJobDetail() {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "TestTrigger", "TestGroup",
            "0 0 9 * * ?", "Daily at 9 AM", LoggingJob.class, true
        );

        // Act
        JobDetail jobDetail = jobTriggerFactory.createJobDetail(jobDef);
        Trigger trigger = jobTriggerFactory.createTrigger(jobDef, jobDetail);

        // Assert
        assertNotNull(jobDetail);
        assertNotNull(trigger);
        assertNotNull(trigger.getStartTime());
    }

    @Test
    void testMultipleJobDetailsAreIndependent() {
        // Arrange
        JobDefinition jobDef1 = new JobDefinition(
            "Job1", "Group1", "Trigger1", "Group1",
            "0 0 * * * ?", "Job 1", LoggingJob.class, true
        );
        JobDefinition jobDef2 = new JobDefinition(
            "Job2", "Group2", "Trigger2", "Group2",
            "0 */30 * * * ?", "Job 2", LoggingJob.class, true
        );

        // Act
        JobDetail detail1 = jobTriggerFactory.createJobDetail(jobDef1);
        JobDetail detail2 = jobTriggerFactory.createJobDetail(jobDef2);

        // Assert
        assertNotEquals(detail1.getKey(), detail2.getKey());
        assertEquals("Job1", detail1.getKey().getName());
        assertEquals("Job2", detail2.getKey().getName());
    }

    @Test
    void testTriggerAssociatedWithCorrectJob() {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "TestTrigger", "TestGroup",
            "0 0 * * * ?", "Test Description", LoggingJob.class, true
        );
        JobDetail jobDetail = jobTriggerFactory.createJobDetail(jobDef);

        // Act
        Trigger trigger = jobTriggerFactory.createTrigger(jobDef, jobDetail);

        // Assert
        assertEquals(jobDetail.getKey(), trigger.getJobKey());
    }

    @Test
    void testJobDetailName() {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "UniqueJobName", "UniqueGroup", "UniqueTrigger", "UniqueGroup",
            "0 0 * * * ?", "Test Description", LoggingJob.class, true
        );

        // Act
        JobDetail jobDetail = jobTriggerFactory.createJobDetail(jobDef);

        // Assert
        assertEquals("UniqueJobName", jobDetail.getKey().getName());
        assertEquals("UniqueGroup", jobDetail.getKey().getGroup());
    }

    @Test
    void testTriggerName() {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "UniqueTriggerName", "UniqueTriggerGroup",
            "0 0 * * * ?", "Test Description", LoggingJob.class, true
        );
        JobDetail jobDetail = jobTriggerFactory.createJobDetail(jobDef);

        // Act
        Trigger trigger = jobTriggerFactory.createTrigger(jobDef, jobDetail);

        // Assert
        assertEquals("UniqueTriggerName", trigger.getKey().getName());
        assertEquals("UniqueTriggerGroup", trigger.getKey().getGroup());
    }
}



