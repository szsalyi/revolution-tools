package com.revolution.tools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main Spring Boot application class for Revolution Tools.
 *
 * This service provides tools and utilities to analyze Evolution games.
 * It includes modules for data processing, game analysis, statistics,
 * and AI-driven insights.
 */
@SpringBootApplication
@EnableCaching
public class RevolutionToolsApplication {

    public static void main(String[] args) {
        SpringApplication.run(RevolutionToolsApplication.class, args);
    }
}
