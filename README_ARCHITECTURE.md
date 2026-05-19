# Spring Quartz Scheduler - Future-Proof Application

A Spring Boot application with a **scalable, maintainable Quartz job scheduling framework** designed for easy extension with new jobs and job groups.

## 📚 Documentation

This project includes comprehensive documentation:

1. **[HELP.md](HELP.md)** - Getting started guide and environment setup
2. **[ARCHITECTURE.md](ARCHITECTURE.md)** - Detailed architecture overview with diagrams and components
3. **[EXTENDING_JOBS.md](EXTENDING_JOBS.md)** - Step-by-step guide to adding new jobs and job groups

**👉 Start here: Read [ARCHITECTURE.md](ARCHITECTURE.md) to understand the design, then [EXTENDING_JOBS.md](EXTENDING_JOBS.md) to add new jobs.**

## 🚀 Quick Start

### Run the Application

```bash
# Development environment
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Staging environment
mvn spring-boot:run -Dspring-boot.run.profiles=staging

# Production environment
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Build the Project

```bash
mvn clean package
```

## 🏗️ Architecture Highlights

### Key Components

- **QuartzJobBase**: Base interface for all jobs (requires: name, group, cron expression)
- **JobDefinition**: Metadata container for job configuration
- **JobRegistry**: Central registry for storing all registered jobs
- **JobRegistrationConfig**: Configuration class where you register jobs ⭐
- **JobTriggerFactory**: Factory for creating Quartz JobDetail and Trigger instances
- **QuartzConfig**: Main Quartz scheduler configuration

### Why This Architecture?

✅ **Extensible**: Add new jobs without modifying framework code  
✅ **Modular**: Each job has its own properties class  
✅ **Maintainable**: Job registration in one place (JobRegistrationConfig)  
✅ **Environment-Aware**: Different configs for dev, staging, prod  
✅ **Spring-Integrated**: Full dependency injection support in jobs  
✅ **Scalable**: Easily support 100+ jobs across multiple groups  

## 📋 Current Jobs

### LoggingJob
- **Group**: WeekdayGroup
- **Schedule**: Every 30 minutes on weekdays (Mon-Fri)
- **Cron**: `0 0/30 * ? * MON-FRI`
- **Properties Prefix**: `job.logging.*`
- **Files**: 
  - Implementation: `src/main/java/com/gcs/quartz/job/LoggingJob.java`
  - Configuration: `src/main/java/com/gcs/quartz/config/LoggingJobProperties.java`

## ➕ Adding a New Job (5 Steps)

### 1. Create Job Class
```java
@Component
public class MyNewJob implements QuartzJobBase {
    private static final String JOB_NAME = "MyNewJob";
    private static final String JOB_GROUP = "MyGroup";
    private static final String CRON_EXPRESSION = "0 0 * * * ?";
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Job logic here
    }
    
    // Implement required methods from QuartzJobBase
}
```

### 2. Create Properties Class (Optional)
```java
@Component
@ConfigurationProperties(prefix = "job.mygroup")
@Getter @Setter
public class MyNewJobProperties {
    private String property1;
    private String property2;
}
```

### 3. Add Properties to Configuration Files
```properties
# In application.properties, application-dev.properties, etc.
job.mygroup.property1=value1
job.mygroup.property2=value2
```

### 4. Register Job in JobRegistrationConfig
```java
@PostConstruct
public void registerAllJobs() {
    registerLoggingJob();
    registerMyNewJob();  // Add this
}

private void registerMyNewJob() {
    JobDefinition myJobDef = new JobDefinition(
        "MyNewJob", "MyGroup", "MyTrigger", "MyGroup",
        "0 0 * * * ?", "My job description",
        MyNewJob.class, true
    );
    jobRegistry.registerJob(myJobDef);
}
```

### 5. That's It! 🎉
Your job will be automatically scheduled and executed.

## 📁 Project Structure

```
src/main/java/com/gcs/quartz/
├── job/
│   ├── QuartzJobBase.java           ← Base interface for all jobs
│   ├── LoggingJob.java              ← Example job implementation
│   └── [YourJob].java               ← Add new jobs here
├── config/
│   ├── QuartzConfig.java            ← Quartz scheduler configuration
│   ├── JobRegistrationConfig.java   ← WHERE YOU ADD JOBS ⭐
│   ├── LoggingJobProperties.java    ← Example job properties
│   └── [YourJobProperties].java     ← Add new property classes
├── registry/
│   └── JobRegistry.java             ← Central job registry
├── factory/
│   ├── JobTriggerFactory.java       ← Creates job details & triggers
│   └── QuartzJobFactory.java        ← Spring integration
├── model/
│   └── JobDefinition.java           ← Job metadata model
└── SpringQuatzDemoApplication.java  ← Main application

src/main/resources/
├── application.properties           ← Base configuration
├── application-dev.properties       ← Dev environment overrides
├── application-staging.properties   ← Staging environment overrides
├── application-prod.properties      ← Production environment overrides
├── quartz-dev.properties            ← Quartz dev config
├── quartz-staging.properties        ← Quartz staging config
└── quartz-prod.properties           ← Quartz production config
```

## 🔧 Configuration Files

### application.properties
Base configuration for all environments. Contains default job properties.

### application-{profile}.properties
Environment-specific overrides for dev, staging, and prod. Examples:
- `job.logging.job-title=Quartz Job [DEV]`
- `job.logging.job-title=Quartz Job [STAGING]`
- `job.logging.job-title=Quartz Job [PROD]`

### quartz-{profile}.properties
Quartz scheduler configuration for each environment:
- **dev**: Uses RAMJobStore (in-memory)
- **staging**: Uses JDBCJobStore with MySQL
- **prod**: Uses JDBCJobStore with MySQL (high performance)

## 🔍 Monitoring & Debugging

### View Registered Jobs
```
Search logs for:
INFO  - Registered job: [JobName] in group: [GroupName]
INFO  - Created job detail and trigger for: [JobName]
```

### Enable Debug Logging
```properties
logging.level.com.gcs.quartz=DEBUG
```

### Common Cron Expressions

| Schedule | Expression |
|----------|-----------|
| Every 30 minutes (weekdays) | `0 0/30 * ? * MON-FRI` |
| Daily at 9:00 AM | `0 0 9 * * ?` |
| Every 15 minutes | `0 */15 * * * ?` |
| Every hour | `0 0 * * * ?` |
| Every Monday at 9 AM | `0 0 9 ? * MON` |
| 1st of month at midnight | `0 0 0 1 * ?` |

**Validate expressions at**: https://crontab.guru/

## 🛠️ Environment Setup

### Prerequisites
- Java 21
- Maven 3.6+
- (Optional) MySQL 5.7+ for staging/prod with persistent job storage

### Installation & Quick Start

**Clone and build:**
```bash
git clone <repository>
cd spring-quatz-demo
mvn clean package
```

**Run without profile (uses embedded H2 in-memory database):**
```bash
java -jar target/spring-quatz-demo-0.0.1-SNAPSHOT.jar
```

**Run with specific profile:**
```bash
# Dev environment (in-memory H2)
java -jar target/spring-quatz-demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# Staging environment (requires MySQL datasource override)
java -jar target/spring-quatz-demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=staging

# Production environment (requires MySQL datasource override)
java -jar target/spring-quatz-demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

**Or run with Maven:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn spring-boot:run -Dspring-boot.run.profiles=staging
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Database Configuration

**Default (Development):**
- Uses embedded H2 in-memory database
- Data is lost when application stops
- Perfect for rapid development and testing

**Staging & Production:**
- Default configuration still uses H2
- To use MySQL, create `application-staging.properties` or `application-prod.properties` with actual datasource settings:
```properties
spring.datasource.url=jdbc:mysql://mysql-server:3306/quartz_staging
spring.datasource.username=quartz_user
spring.datasource.password=secure_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=validate
```

### Installation
```bash
git clone <repository>
cd spring-quatz-demo
mvn clean install
```


## 📊 Job Groups Support

The framework supports unlimited job groups for organizing related jobs:

```
WeekdayGroup/
├── LoggingJob

EmailGroup/
├── EmailNotificationJob
├── EmailReportJob

DataSyncGroup/
├── DatabaseSyncJob
├── APIDataFetchJob

ReportingGroup/
├── DailyReportJob
├── WeeklyReportJob
└── MonthlyReportJob

... and more!
```

## 🎓 Learning Path

1. **Read** [HELP.md](HELP.md) - Environment setup and running the app
2. **Review** [ARCHITECTURE.md](ARCHITECTURE.md) - Understand the design principles
3. **Follow** [EXTENDING_JOBS.md](EXTENDING_JOBS.md) - Add your first job
4. **Explore** Source code in `src/main/java/com/gcs/quartz/`
5. **Experiment** Create multiple jobs to understand the patterns

## 🚀 Best Practices

### DO:
✅ Store job name, group, and cron as constants in job class  
✅ Create separate properties class for each job group  
✅ Override properties in environment-specific files  
✅ Keep jobs stateless and idempotent  
✅ Add comprehensive logging  
✅ Document job purpose and schedule  
✅ Use @Component and @Autowired for dependencies  

### DON'T:
❌ Edit QuartzConfig or JobRegistry (framework code)  
❌ Hardcode properties in job classes  
❌ Create circular job dependencies  
❌ Store state in job instances  
❌ Ignore exception handling  
❌ Mix job types in one properties class  

## 📞 Support & Troubleshooting

### Job Not Running?
1. Check if registered in JobRegistrationConfig
2. Verify cron expression correctness
3. Check if job is enabled (last parameter in JobDefinition)
4. Review application logs for errors

### Properties Not Loading?
1. Verify @ConfigurationProperties annotation
2. Check property names use kebab-case (e.g., job-name)
3. Ensure properties defined in correct application-*.properties file
4. Verify property class has @Component annotation

### Spring Dependency Injection Not Working?
1. Ensure job class has @Component annotation
2. Verify dependency parameter in constructor
3. Check QuartzJobFactory is properly configured
4. Review Spring component scan configuration

## 📝 License

This project is licensed under the MIT License - see LICENSE file for details.

## 🎉 Ready to Extend?

The framework is ready for production-grade scheduling! To add your first job:

1. Open `src/main/java/com/gcs/quartz/config/JobRegistrationConfig.java`
2. Follow the comments in the TODO section
3. Reference [EXTENDING_JOBS.md](EXTENDING_JOBS.md) for detailed examples

Happy scheduling! 🗓️

