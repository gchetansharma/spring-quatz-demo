package com.gcs.quartz.registry;

import com.gcs.quartz.job.LoggingJob;
import com.gcs.quartz.model.JobDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class JobRegistryTest {

    private JobRegistry jobRegistry;

    @BeforeEach
    void setUp() {
        jobRegistry = new JobRegistry();
    }

    @Test
    void testRegisterJob() {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "TestTrigger", "TestGroup",
            "0 0 * * * ?", "Test Description", LoggingJob.class, true
        );

        // Act
        jobRegistry.registerJob(jobDef);

        // Assert
        JobDefinition retrieved = jobRegistry.getJob("TestGroup", "TestJob");
        assertNotNull(retrieved);
        assertEquals("TestJob", retrieved.getJobName());
        assertEquals("TestGroup", retrieved.getJobGroup());
    }

    @Test
    void testGetJob_Found() {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "TestTrigger", "TestGroup",
            "0 0 * * * ?", "Test Description", LoggingJob.class, true
        );
        jobRegistry.registerJob(jobDef);

        // Act
        JobDefinition retrieved = jobRegistry.getJob("TestGroup", "TestJob");

        // Assert
        assertNotNull(retrieved);
        assertEquals("TestJob", retrieved.getJobName());
    }

    @Test
    void testGetJob_NotFound() {
        // Act
        JobDefinition retrieved = jobRegistry.getJob("NonExistent", "NonExistent");

        // Assert
        assertNull(retrieved);
    }

    @Test
    void testGetAllJobs() {
        // Arrange
        JobDefinition jobDef1 = new JobDefinition(
            "TestJob1", "TestGroup", "TestTrigger1", "TestGroup",
            "0 0 * * * ?", "Test Description 1", LoggingJob.class, true
        );
        JobDefinition jobDef2 = new JobDefinition(
            "TestJob2", "TestGroup2", "TestTrigger2", "TestGroup2",
            "0 */15 * * * ?", "Test Description 2", LoggingJob.class, true
        );
        jobRegistry.registerJob(jobDef1);
        jobRegistry.registerJob(jobDef2);

        // Act
        Collection<JobDefinition> allJobs = jobRegistry.getAllJobs();

        // Assert
        assertEquals(2, allJobs.size());
    }

    @Test
    void testGetAllJobs_Empty() {
        // Act
        Collection<JobDefinition> allJobs = jobRegistry.getAllJobs();

        // Assert
        assertEquals(0, allJobs.size());
    }

    @Test
    void testUpdateJob() {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "TestTrigger", "TestGroup",
            "0 0 * * * ?", "Test Description", LoggingJob.class, true
        );
        jobRegistry.registerJob(jobDef);

        // Update the cron expression
        jobDef.setCronExpression("0 */15 * * * ?");
        jobDef.setDescription("Updated Description");

        // Act
        jobRegistry.updateJob(jobDef);

        // Assert
        JobDefinition retrieved = jobRegistry.getJob("TestGroup", "TestJob");
        assertNotNull(retrieved);
        assertEquals("0 */15 * * * ?", retrieved.getCronExpression());
        assertEquals("Updated Description", retrieved.getDescription());
    }

    @Test
    void testGetEnabledJobs() {
        // Arrange
        JobDefinition enabledJob = new JobDefinition(
            "EnabledJob", "TestGroup", "EnabledTrigger", "TestGroup",
            "0 0 * * * ?", "Enabled Job", LoggingJob.class, true
        );
        JobDefinition disabledJob = new JobDefinition(
            "DisabledJob", "TestGroup", "DisabledTrigger", "TestGroup",
            "0 0 * * * ?", "Disabled Job", LoggingJob.class, false
        );
        jobRegistry.registerJob(enabledJob);
        jobRegistry.registerJob(disabledJob);

        // Act
        Collection<JobDefinition> enabledJobs = jobRegistry.getEnabledJobs();

        // Assert
        assertEquals(1, enabledJobs.size());
        assertTrue(enabledJobs.stream().anyMatch(j -> j.getJobName().equals("EnabledJob")));
    }

    @Test
    void testGetJobsByGroup() {
        // Arrange
        JobDefinition jobDef1 = new JobDefinition(
            "Job1", "Group1", "Trigger1", "Group1",
            "0 0 * * * ?", "Job 1", LoggingJob.class, true
        );
        JobDefinition jobDef2 = new JobDefinition(
            "Job2", "Group1", "Trigger2", "Group1",
            "0 */30 * * * ?", "Job 2", LoggingJob.class, true
        );
        JobDefinition jobDef3 = new JobDefinition(
            "Job3", "Group2", "Trigger3", "Group2",
            "0 0 * * * ?", "Job 3", LoggingJob.class, true
        );
        jobRegistry.registerJob(jobDef1);
        jobRegistry.registerJob(jobDef2);
        jobRegistry.registerJob(jobDef3);

        // Act
        Collection<JobDefinition> group1Jobs = jobRegistry.getJobsByGroup("Group1");

        // Assert
        assertEquals(2, group1Jobs.size());
    }

    @Test
    void testJobExists() {
        // Arrange
        JobDefinition jobDef = new JobDefinition(
            "TestJob", "TestGroup", "TestTrigger", "TestGroup",
            "0 0 * * * ?", "Test Description", LoggingJob.class, true
        );
        jobRegistry.registerJob(jobDef);

        // Act & Assert
        assertTrue(jobRegistry.jobExists("TestGroup", "TestJob"));
        assertFalse(jobRegistry.jobExists("NonExistent", "NonExistent"));
    }

    @Test
    void testGetTotalJobs() {
        // Arrange
        JobDefinition jobDef1 = new JobDefinition(
            "Job1", "Group1", "Trigger1", "Group1",
            "0 0 * * * ?", "Job 1", LoggingJob.class, true
        );
        JobDefinition jobDef2 = new JobDefinition(
            "Job2", "Group2", "Trigger2", "Group2",
            "0 */30 * * * ?", "Job 2", LoggingJob.class, true
        );
        jobRegistry.registerJob(jobDef1);
        jobRegistry.registerJob(jobDef2);

        // Act
        int total = jobRegistry.getTotalJobs();

        // Assert
        assertEquals(2, total);
    }

    @Test
    void testRegisterMultipleJobsInDifferentGroups() {
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
        jobRegistry.registerJob(jobDef1);
        jobRegistry.registerJob(jobDef2);

        // Assert
        JobDefinition retrieved1 = jobRegistry.getJob("Group1", "Job1");
        JobDefinition retrieved2 = jobRegistry.getJob("Group2", "Job2");

        assertNotNull(retrieved1);
        assertNotNull(retrieved2);
        assertEquals("Job1", retrieved1.getJobName());
        assertEquals("Job2", retrieved2.getJobName());
    }
}


