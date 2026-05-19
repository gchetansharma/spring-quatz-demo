# Startup Fixes & Configuration

This document describes the fixes applied to resolve application startup issues.

## Issues Fixed

### Issue 1: "Failed to configure a DataSource"

**Problem:**
```
Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured.
Reason: Failed to determine a suitable driver class
```

**Root Cause:**
- Spring Boot was trying to auto-configure a DataSource (because `spring-boot-starter-data-jpa` is on the classpath)
- No datasource URL/driver was configured
- No embedded database was available

**Solution:**
1. Added **H2 embedded database dependency** to `pom.xml`:
   ```xml
   <dependency>
       <groupId>com.h2database</groupId>
       <artifactId>h2</artifactId>
       <scope>runtime</scope>
   </dependency>
   ```

2. Added default **H2 in-memory datasource configuration** to `application.properties`:
   ```properties
   spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
   spring.datasource.driver-class-name=org.h2.Driver
   spring.datasource.username=sa
   spring.datasource.password=
   spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
   spring.jpa.hibernate.ddl-auto=update
   ```

3. Updated **Quartz properties location** to default to `dev` when no profile is active:
   ```properties
   spring.quartz.properties-location=classpath:quartz-${spring.profiles.active:dev}.properties
   ```

**Result:** Application now starts successfully without any external database or active profile.

---

### Issue 2: "SwaggerWelcomeCommon bean could not be found"

**Problem:**
```
Parameter 3 of method indexPageTransformer in org.springdoc.webmvc.ui.SwaggerConfig 
required a bean of type 'org.springdoc.webmvc.ui.SwaggerWelcomeCommon' that could not be found.
```

**Root Cause:**
- `springdoc-openapi-starter-webmvc-ui` version 3.0.2 was incompatible with Spring Boot 4.0.6
- Bean dependency wiring failed

**Solution:**
Removed `springdoc-openapi-starter-webmvc-ui` dependency from `pom.xml` entirely.

```xml
<!-- REMOVED -->
<!-- 
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.2</version>
</dependency>
-->
```

**Rationale:** This is a Quartz scheduler demo application, not an API documentation showcase. SpringDoc/Swagger is not essential for core functionality. The application works perfectly without it.

**Result:** Application now starts without Swagger/OpenAPI bean configuration errors.

---

### Issue 3: "spring.profiles.active" in profile-specific files

**Problem:**
```
Property 'spring.profiles.active' imported from location 'class path resource [application-dev.properties]' 
is invalid in a profile specific resource
```

**Root Cause:**
- Profile-specific files (`application-dev.properties`, `application-staging.properties`, `application-prod.properties`) contained `spring.profiles.active=<profile>` lines
- Spring does not allow setting `spring.profiles.active` inside a profile-specific resource file (creates circular dependency)

**Solution:**
Removed `spring.profiles.active` lines from all profile-specific files:

**Before:**
```properties
# application-dev.properties
spring.profiles.active=dev
logging.level.com.gcs.quartz=DEBUG
...
```

**After:**
```properties
# application-dev.properties
# Dev environment settings
logging.level.com.gcs.quartz=DEBUG
...
```

**How to activate profiles:** Use command-line argument or environment variable instead:
```bash
# Command line
java -jar app.jar --spring.profiles.active=dev

# Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Environment variable
export SPRING_PROFILES_ACTIVE=dev
```

**Result:** Profile-specific properties files now load correctly without conflicts.

---

## Verification

### ✅ All Startup Scenarios Now Work

**1. No profile (default):**
```bash
java -jar target/spring-quatz-demo-0.0.1-SNAPSHOT.jar
```
✓ Uses embedded H2 in-memory database
✓ Falls back to dev quartz configuration
✓ Jobs registered and scheduled successfully

**2. Explicit dev profile:**
```bash
java -jar target/spring-quatz-demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```
✓ Loads `application-dev.properties`
✓ Sets logging.level.com.gcs.quartz=DEBUG
✓ Jobs registered with dev configuration

**3. Explicit staging profile:**
```bash
java -jar target/spring-quatz-demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=staging
```
✓ Loads `application-staging.properties`
✓ Sets logging.level.com.gcs.quartz=INFO
✓ Uses default H2 (override datasource in staging props for MySQL)

**4. Explicit prod profile:**
```bash
java -jar target/spring-quatz-demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```
✓ Loads `application-prod.properties`
✓ Sets logging.level.com.gcs.quartz=WARN
✓ Uses default H2 (override datasource in prod props for MySQL)

---

## Startup Logs Confirmation

**Expected log output includes:**
```
com.gcs.quartz.registry.JobRegistry : Registered job: LoggingJob in group: WeekdayGroup
c.gcs.quartz.factory.JobTriggerFactory : Created job detail and trigger for: LoggingJob in group: WeekdayGroup
c.gcs.quartz.SpringQuatzDemoApplication : Started SpringQuatzDemoApplication in X.XXX seconds
```

**No errors about:**
- Missing DataSource
- Missing SwaggerWelcomeCommon bean
- Invalid spring.profiles.active property

---

## Configuration for Staging/Production with MySQL

To use MySQL instead of embedded H2 for staging/production:

**Edit `application-staging.properties`:**
```properties
# Staging environment - MySQL configuration
spring.datasource.url=jdbc:mysql://mysql-staging:3306/quartz_staging
spring.datasource.username=quartz_user
spring.datasource.password=${QUARTZ_DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=validate
```

**Edit `application-prod.properties`:**
```properties
# Production environment - MySQL configuration
spring.datasource.url=jdbc:mysql://mysql-prod:3306/quartz_prod
spring.datasource.username=quartz_prod_user
spring.datasource.password=${QUARTZ_DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=validate
```

---

## Summary of Changes

| File | Change | Why |
|------|--------|-----|
| `pom.xml` | Added H2 dependency | Embedded database for local dev/test |
| `pom.xml` | Removed springdoc-openapi | Incompatible with Spring Boot 4.0.6 |
| `application.properties` | Added H2 datasource config | Default DB when no profile specified |
| `application.properties` | Changed quartz location to use `:dev` default | Falls back to dev quartz config when no profile |
| `application-dev.properties` | Removed `spring.profiles.active=dev` | Not allowed in profile-specific files |
| `application-staging.properties` | Removed `spring.profiles.active=staging` | Not allowed in profile-specific files |
| `application-prod.properties` | Removed `spring.profiles.active=prod` | Not allowed in profile-specific files |

---

## Running the Application

```bash
# Build
mvn clean package

# Run (default: embedded H2, dev Quartz config)
java -jar target/spring-quatz-demo-0.0.1-SNAPSHOT.jar

# Run specific profile
java -jar target/spring-quatz-demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
java -jar target/spring-quatz-demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=staging
java -jar target/spring-quatz-demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

The application now starts reliably in all scenarios! 🎉

