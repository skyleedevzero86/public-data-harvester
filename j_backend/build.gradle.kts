plugins {
    java
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

group = "com.antock"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["springAiVersion"] = "1.0.0-M6"
extra["testcontainersVersion"] = "1.20.1"

val activeProfiles = project.findProperty("spring.profiles.active")?.toString()
    ?: System.getProperty("spring.profiles.active")
    ?: System.getenv("SPRING_PROFILES_ACTIVE")
    ?: "dev"

val includeRedis = activeProfiles.contains("dev") ||
        project.hasProperty("redis") ||
        project.hasProperty("dev")

println("Active profiles: $activeProfiles")
println("Include Redis: $includeRedis")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    implementation("redis.clients:jedis:4.4.3")

    implementation("org.springframework:spring-aspects")
    implementation("org.springframework.retry:spring-retry")

    implementation("org.springframework.security:spring-security-oauth2-client")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("org.springframework.security:spring-security-taglibs")

    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

    implementation("org.postgresql:postgresql")
    implementation("com.h2database:h2")

    implementation("org.apache.tomcat.embed:tomcat-embed-jasper")
    implementation("jakarta.servlet:jakarta.servlet-api")

    implementation("jakarta.servlet.jsp:jakarta.servlet.jsp-api:3.1.1")
    implementation("org.eclipse.jdt:ecj:3.33.0")

    implementation("jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api:3.0.0")
    implementation("org.glassfish.web:jakarta.servlet.jsp.jstl:3.0.1")

    implementation("com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5")
    implementation("org.jasypt:jasypt:1.9.3")

    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.google.api-client:google-api-client:2.2.0")

    implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:8.0.1")

    implementation("commons-io:commons-io:2.15.1")

    implementation("io.minio:minio:8.5.7")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.apache.poi:poi-ooxml:5.2.4")

    implementation("org.webjars:jquery:3.6.0")
    implementation("org.webjars:bootstrap:5.3.7")

    implementation("io.micrometer:micrometer-registry-prometheus")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:postgresql")

    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("net.bytebuddy:byte-buddy-agent:1.14.11")
    implementation ("org.springframework.boot:spring-boot-starter-mail")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    finalizedBy(tasks.jacocoTestCoverageVerification)
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.05".toBigDecimal()
            }
        }
    }
}

tasks.register("runDev") {
    group = "application"
    description = "Run with dev profile (includes Redis)"
    dependsOn("build")
    doLast {
        project.exec {
            commandLine("java", "-jar", "-Dspring.profiles.active=dev",
                "build/libs/${project.name}-${project.version}.jar")
        }
    }
}

tasks.register("runProd") {
    group = "application"
    description = "Run with prod profile (excludes Redis)"
    dependsOn("build")
    doLast {
        project.exec {
            commandLine("java", "-jar", "-Dspring.profiles.active=prod",
                "build/libs/${project.name}-${project.version}.jar")
        }
    }
}