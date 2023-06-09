package com.hierarchy.gmbh.api.employee.relationship.security.config;

import com.hierarchy.gmbh.api.employee.relationship.jpa.repository.ApiTokenRepository;
import com.hierarchy.gmbh.api.employee.relationship.security.filter.HierarchyGmbHAuthenticationFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class HierarchyGmbHAuthenticationConfig {
    @Autowired private ApiTokenRepository apiTokenRepository;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/**")
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(
                        new HierarchyGmbHAuthenticationFilter(apiTokenRepository),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
