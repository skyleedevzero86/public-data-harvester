package com.antock.corp;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;

@TestConfiguration
@EntityScan(basePackages = {
        "com.antock.api.coseller.domain",
        "com.antock.api.member.domain",
        "com.antock.api.file.domain",
        "com.antock.api.csv.domain"
})
@EnableJpaRepositories(basePackages = {
        "com.antock.api.coseller.infrastructure",
        "com.antock.api.member.infrastructure",
        "com.antock.api.file.infrastructure",
        "com.antock.api.csv.infrastructure"
})
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.show-sql=true",
        "spring.jpa.properties.hibernate.format_sql=true"
})
public class TestConfig {
}