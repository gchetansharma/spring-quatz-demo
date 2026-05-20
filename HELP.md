# Getting Started

### Running in Different Environments

This application supports dev, staging, and prod environments with profile-specific configurations.

**Default (No Profile):**
- Uses embedded H2 file-based database (persists to `./data/quartz` directory)
- Falls back to dev quartz configuration
- Quartz jobs are persisted to H2 database (JDBCJobStore)
- Ideal for local development and testing

```bash
java -jar target/spring-quartz-demo-0.0.1-SNAPSHOT.jar
```

**To run with a specific profile:**

```bash
# Dev Environment
java -jar target/spring-quartz-demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
# Or with Maven:
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Staging Environment
java -jar target/spring-quartz-demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=staging
mvn spring-boot:run -Dspring-boot.run.profiles=staging

# Production Environment
java -jar target/spring-quartz-demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Each profile uses environment-specific configuration from `application-{profile}.properties` files.

Quartz scheduler configuration:
- `quartz-dev.properties`: JDBCJobStore with H2 file-based database for persistent job storage
- `quartz-staging.properties`: Uses JDBCJobStore with MySQL (configure datasource separately)
- `quartz-prod.properties`: Uses JDBCJobStore with MySQL (configure datasource separately)

### Quartz Jobs

#### Overview

This application uses a **future-proof, registry-based architecture** for managing Quartz jobs. The architecture is designed to support multiple job groups and jobs with separate property management for scalability and maintainability.

**Architecture Components:**
- **JobRegistry**: Central registry for all job definitions
- **JobDefinition**: Metadata container for job configuration
- **JobRegistrationConfig**: Configuration class for registering jobs
- **JobTriggerFactory**: Factory for creating job details and triggers
- **QuartzJobBase**: Base interface for all jobs

**Key Benefits:**
- ✅ Easy to add new jobs without modifying core configuration
- ✅ Support for multiple job groups
- ✅ Environment-specific properties per job
- ✅ Spring dependency injection in jobs
- ✅ Dynamic job registration and scheduling
- ✅ Enable/disable jobs without code changes

#### LoggingJob

The `LoggingJob` is a cron-scheduled job that logs a message every 30 minutes on all weekdays (Monday through Friday).

**Features:**
- **Schedule**: Runs every 30 minutes on weekdays (Mon-Fri)
- **Cron Expression**: `0 0/30 * ? * MON-FRI`
- **Functionality**: Prints timestamp, job details, and trigger information to the application logs
- **Configurable**: All string literals are externalized as properties and can be customized per environment

**Sample Log Output:**
```
========================================
Quartz Job Executed - Weekday Scheduler
Execution Time: 2026-05-14 09:00:00
Job Name: LoggingJob
Job Group: WeekdayGroup
Trigger Name: LoggingTrigger
Next Fire Time: Wed May 14 09:30:00 EDT 2026
========================================
```

**Configurable Properties:**

The LoggingJob uses the following externalized properties (prefix: `job.logging`):

| Property | Default Value | Description |
|----------|---------------|-------------|
| `job.logging.log-separator` | `========================================` | Separator line for log output |
| `job.logging.job-title` | `Quartz Job Executed - Weekday Scheduler` | Job execution title message |
| `job.logging.execution-time-label` | `Execution Time:` | Label for execution time |
| `job.logging.job-name-label` | `Job Name:` | Label for job name |
| `job.logging.job-group-label` | `Job Group:` | Label for job group |
| `job.logging.trigger-name-label` | `Trigger Name:` | Label for trigger name |
| `job.logging.next-fire-time-label` | `Next Fire Time:` | Label for next fire time |
| `job.logging.date-time-format` | `yyyy-MM-dd HH:mm:ss` | DateTime format pattern |

**Customizing Properties:**

To customize the LoggingJob output, modify the properties in the appropriate environment file:

- `application.properties`: Base configuration (applies to all profiles)
- `application-dev.properties`: Dev environment overrides
- `application-staging.properties`: Staging environment overrides
- `application-prod.properties`: Prod environment overrides

Example: To change the job title for dev environment, edit `application-dev.properties`:
```properties
job.logging.job-title=My Custom Job Title [DEV]
```

**Files:**
- `src/main/java/com/gcs/quartz/job/LoggingJob.java`: Job implementation
- `src/main/java/com/gcs/quartz/config/QuartzConfig.java`: Quartz scheduler configuration
- `src/main/java/com/gcs/quartz/config/LoggingJobProperties.java`: Properties configuration class
- `src/main/java/com/gcs/quartz/factory/QuartzJobFactory.java`: Custom job factory for Spring dependency injection

### REST API for Job Management

The application provides a REST API to view and manage Quartz jobs at runtime. You can interact with the API using tools like `curl` or through the interactive **Swagger UI**.

#### Interactive Documentation (Swagger UI)

Swagger UI provides a user-friendly interface to explore and test the API endpoints.

- **URL**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Features**:
    - View all available endpoints and their descriptions.
    - Inspect request and response schemas.
    - Test endpoints directly from the browser using the "Try it out" button.

#### Endpoints for managing jobs are organized into two categories: viewing jobs and managing jobs.

##### View Jobs

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/jobs` | List all registered jobs |
| `GET` | `/api/jobs/{group}/{name}` | Get details for a specific job |

##### Manage Jobs

| Method | Endpoint | Description |
|--------|----------|-------------|
| `PUT` | `/api/jobs/{group}/{name}` | Update job configuration (cron expression, description, enabled status) |
| `POST` | `/api/jobs/{group}/{name}/start` | Manually trigger a job to run immediately (one-time execution) |
| `POST` | `/api/jobs/{group}/{name}/pause` | Pause the job's scheduled trigger (job remains registered) |
| `POST` | `/api/jobs/{group}/{name}/resume` | Resume a paused job's trigger |
| `DELETE` | `/api/jobs/{group}/{name}` | Stop and permanently delete a job from the scheduler |

#### Examples

##### Example 1: List All Jobs
**Request:**
```bash
curl http://localhost:8080/api/jobs
```

**Response:**
```json
[
  {
    "jobName": "LoggingJob",
    "jobGroup": "WeekdayGroup",
    "triggerName": "LoggingTrigger",
    "triggerGroup": "WeekdayGroup",
    "cronExpression": "0 0/30 * ? * MON-FRI",
    "description": "Logs message every 30 minutes on weekdays",
    "jobClassName": "com.gcs.quartz.job.LoggingJob",
    "enabled": true
  }
]
```

##### Example 2: Get Job Details
**Request:**
```bash
curl http://localhost:8080/api/jobs/WeekdayGroup/LoggingJob
```

##### Example 3: Update a Job
Update the cron expression, description, or enabled status of a job. Changes are applied immediately to the Quartz scheduler.

**Request:**
```bash
curl -X PUT http://localhost:8080/api/jobs/WeekdayGroup/LoggingJob \
  -H "Content-Type: application/json" \
  -d '{
    "cronExpression": "0 */15 * * * ?",
    "enabled": true,
    "description": "Updated logging job to run every 15 minutes"
  }'
```

**Response:**
```
200 OK
```

##### Example 4: Manually Start/Trigger a Job
Immediately execute the job one time, regardless of the schedule.

**Request:**
```bash
curl -X POST http://localhost:8080/api/jobs/WeekdayGroup/LoggingJob/start
```

**Response:**
```json
"Job triggered successfully"
```

##### Example 5: Pause a Job
Pause the job's trigger so it stops executing on schedule. The job remains registered and can be resumed.

**Request:**
```bash
curl -X POST http://localhost:8080/api/jobs/WeekdayGroup/LoggingJob/pause
```

**Response:**
```json
"Job paused successfully"
```

##### Example 6: Resume a Paused Job
Resume a previously paused job's trigger to restore scheduled execution.

**Request:**
```bash
curl -X POST http://localhost:8080/api/jobs/WeekdayGroup/LoggingJob/resume
```

**Response:**
```json
"Job resumed successfully"
```

##### Example 7: Stop and Delete a Job
Permanently stop and remove a job from the scheduler. The job definition remains in the registry but is no longer scheduled.

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/jobs/WeekdayGroup/LoggingJob
```

**Response:**
```json
"Job stopped and deleted successfully"
```

### H2 Database Console

The application includes the **H2 Database Console** for viewing and managing the Quartz job data stored in the H2 database during development.

#### Accessing H2 Console

1. **Start the application** (dev profile):
   ```bash
   java -jar target/spring-quartz-demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
   # Or with Maven:
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

2. **Open H2 Console in your browser:**
   - **URL**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

3. **Login with the following credentials:**
   - **JDBC URL**: `jdbc:h2:./data/quartz`
   - **Username**: `sa`
   - **Password**: (leave empty)

4. **Click "Connect"** to access the database

#### Viewing Quartz Job Data

Once connected to the H2 Console, you can view the following Quartz tables:

| Table | Purpose |
|-------|---------|
| `QRTZ_JOB_DETAILS` | Stores job definitions and metadata |
| `QRTZ_TRIGGERS` | Stores trigger schedules and states |
| `QRTZ_CRON_TRIGGERS` | Stores cron expressions for triggers |
| `QRTZ_FIRED_TRIGGERS` | Stores history of fired triggers |
| `QRTZ_SCHEDULER_STATE` | Stores scheduler instance information |
| `QRTZ_LOCKS` | Stores distributed locking information |

#### Example SQL Queries

**View all registered jobs:**
```sql
SELECT * FROM QRTZ_JOB_DETAILS;
```

**View all triggers:**
```sql
SELECT * FROM QRTZ_TRIGGERS;
```

**View cron expressions for triggers:**
```sql
SELECT TG.TRIGGER_NAME, TG.TRIGGER_GROUP, CT.CRON_EXPRESSION 
FROM QRTZ_TRIGGERS TG 
JOIN QRTZ_CRON_TRIGGERS CT ON TG.SCHED_NAME = CT.SCHED_NAME 
  AND TG.TRIGGER_NAME = CT.TRIGGER_NAME 
  AND TG.TRIGGER_GROUP = CT.TRIGGER_GROUP;
```

**View trigger execution history:**
```sql
SELECT * FROM QRTZ_FIRED_TRIGGERS ORDER BY FIRED_TIME DESC;
```

#### Data Persistence

- **Location**: `./data/quartz` directory (relative to application root)
- **Persistence**: H2 database file persists between application restarts
- **Clearing Data**: Delete the `./data` directory to reset the database
- **Automatic Schema**: Quartz tables are automatically created on first startup from `schema.sql`

### Adding New Jobs

To extend the application with new jobs and job groups, refer to **[EXTENDING_JOBS.md](EXTENDING_JOBS.md)** for detailed instructions on:

- Creating new job classes
- Adding job-specific properties
- Creating new job groups
- Registering jobs with the JobRegistry
- Environment-specific configuration
- Cron expression examples

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.6/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.0.6/maven-plugin/build-image.html)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/4.0.6/reference/actuator/index.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.0.6/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/4.0.6/reference/using/devtools.html)
* [Quartz Scheduler](https://docs.spring.io/spring-boot/4.0.6/reference/io/quartz.html)
* [SpringDoc OpenAPI](https://springdoc.org/)
* [Spring Web](https://docs.spring.io/spring-boot/4.0.6/reference/web/servlet.html)

### Guides

The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Accessing data with MySQL](https://spring.io/guides/gs/accessing-data-mysql/)
* [SpringDoc OpenAPI](https://github.com/springdoc/springdoc-openapi-demos/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the
parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

