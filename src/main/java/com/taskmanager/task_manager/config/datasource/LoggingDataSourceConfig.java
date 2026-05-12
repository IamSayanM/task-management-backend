package com.taskmanager.task_manager.config.datasource;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories(basePackages = "com.taskmanager.task_manager.repository.logging", entityManagerFactoryRef = "loggingEntityManagerFactory", transactionManagerRef = "loggingTransactionManager")
public class LoggingDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.logging-datasource")
    public DataSourceProperties loggingDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource loggingDataSource() {
        return loggingDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean loggingEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(loggingDataSource())
                .packages("com.taskmanager.task_manager.entity.logging") // logging entities
                .persistenceUnit("logging")
                .build();
    }

    @Bean(name = "loggingTransactionManager")
    public PlatformTransactionManager loggingTransactionManager(
            @Qualifier("loggingEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}