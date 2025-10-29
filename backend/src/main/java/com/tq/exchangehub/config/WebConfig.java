package com.tq.exchangehub.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class WebConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.getAllowedOrigins());
        configuration.setAllowedMethods(properties.getAllowedMethods());
        configuration.setAllowedHeaders(properties.getAllowedHeaders());

        Boolean allowCredentials = properties.getAllowCredentials();
        if (allowCredentials != null) {
            configuration.setAllowCredentials(allowCredentials);
        }

        List<String> exposedHeaders = properties.getExposedHeaders();
        if (!CollectionUtils.isEmpty(exposedHeaders)) {
            configuration.setExposedHeaders(exposedHeaders);
        }

        configuration.setMaxAge(Duration.ofSeconds(Math.max(properties.getMaxAge(), 0)));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
