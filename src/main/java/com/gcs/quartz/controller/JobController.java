package com.gcs.quartz.controller;

import com.gcs.quartz.dto.JobInfoDTO;
import com.gcs.quartz.dto.JobUpdateRequestDTO;
import com.gcs.quartz.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.quartz.SchedulerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Job Management", description = "Endpoints for viewing and managing Quartz jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    @Operation(summary = "List all registered jobs", description = "Returns a list of all jobs registered in the system")
    public List<JobInfoDTO> getAllJobs() {
        return jobService.getAllJobs();
    }

    @GetMapping("/{group}/{name}")
    @Operation(summary = "Get job details", description = "Returns detailed information about a specific job")
    public ResponseEntity<JobInfoDTO> getJob(
            @Parameter(description = "The group name of the job") @PathVariable String group,
            @Parameter(description = "The name of the job") @PathVariable String name) {
        return jobService.getJob(group, name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{group}/{name}")
    @Operation(summary = "Update a job", description = "Updates the cron expression, description, or enabled status of a job")
    public ResponseEntity<?> updateJob(
            @Parameter(description = "The group name of the job") @PathVariable String group,
            @Parameter(description = "The name of the job") @PathVariable String name,
            @RequestBody JobUpdateRequestDTO request) {
        try {
            boolean updated = jobService.updateJob(group, name, request);
            if (updated) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (SchedulerException e) {
            return ResponseEntity.internalServerError().body("Error updating job: " + e.getMessage());
        }
    }

    @PostMapping("/{group}/{name}/start")
    @Operation(summary = "Manually start a job", description = "Triggers the job to run immediately (one-time execution)")
    public ResponseEntity<?> startJob(
            @Parameter(description = "The group name of the job") @PathVariable String group,
            @Parameter(description = "The name of the job") @PathVariable String name) {
        try {
            boolean started = jobService.startJob(group, name);
            if (started) {
                return ResponseEntity.ok().body("Job triggered successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job not found or not scheduled");
            }
        } catch (SchedulerException e) {
            return ResponseEntity.internalServerError().body("Error starting job: " + e.getMessage());
        }
    }

    @PostMapping("/{group}/{name}/pause")
    @Operation(summary = "Pause a job", description = "Pauses the job's trigger (scheduled execution stops but job remains registered)")
    public ResponseEntity<?> pauseJob(
            @Parameter(description = "The group name of the job") @PathVariable String group,
            @Parameter(description = "The name of the job") @PathVariable String name) {
        try {
            boolean paused = jobService.pauseJob(group, name);
            if (paused) {
                return ResponseEntity.ok().body("Job paused successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job not found or not scheduled");
            }
        } catch (SchedulerException e) {
            return ResponseEntity.internalServerError().body("Error pausing job: " + e.getMessage());
        }
    }

    @PostMapping("/{group}/{name}/resume")
    @Operation(summary = "Resume a paused job", description = "Resumes a job that was previously paused")
    public ResponseEntity<?> resumeJob(
            @Parameter(description = "The group name of the job") @PathVariable String group,
            @Parameter(description = "The name of the job") @PathVariable String name) {
        try {
            boolean resumed = jobService.resumeJob(group, name);
            if (resumed) {
                return ResponseEntity.ok().body("Job resumed successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job not found or not scheduled");
            }
        } catch (SchedulerException e) {
            return ResponseEntity.internalServerError().body("Error resuming job: " + e.getMessage());
        }
    }

    @DeleteMapping("/{group}/{name}")
    @Operation(summary = "Stop and delete a job", description = "Permanently stops and removes a job from the scheduler")
    public ResponseEntity<?> stopJob(
            @Parameter(description = "The group name of the job") @PathVariable String group,
            @Parameter(description = "The name of the job") @PathVariable String name) {
        try {
            boolean stopped = jobService.stopJob(group, name);
            if (stopped) {
                return ResponseEntity.ok().body("Job stopped and deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job not found or not scheduled");
            }
        } catch (SchedulerException e) {
            return ResponseEntity.internalServerError().body("Error stopping job: " + e.getMessage());
        }
    }
}
