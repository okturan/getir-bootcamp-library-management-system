package com.okturan.getirbootcamplibrarymanagementsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for the application.
 * Enables Spring Data Web support to ensure a stable JSON structure for paginated responses.
 * This addresses the warning: "Serializing PageImpl instances as-is is not supported"
 */
@Configuration
@EnableSpringDataWebSupport
public class WebConfig implements WebMvcConfigurer {
    // No additional configuration needed
}
