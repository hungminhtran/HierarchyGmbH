package com.hierarchy.gmbh.api.employee.relationship.security.filter;

import com.hierarchy.gmbh.api.employee.relationship.jpa.repository.ApiTokenRepository;
import com.hierarchy.gmbh.api.employee.relationship.security.utils.HierarchyGmbHAuthenticationUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HierarchyGmbHAuthenticationFilter extends GenericFilterBean {
    private static final Logger LOGGER =
            LogManager.getLogger(HierarchyGmbHAuthenticationFilter.class);
    private final ApiTokenRepository apiTokenRepository;

    public HierarchyGmbHAuthenticationFilter(ApiTokenRepository apiTokenRepository) {
        this.apiTokenRepository = apiTokenRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            SecurityContextHolder.getContext()
                    .setAuthentication(
                            HierarchyGmbHAuthenticationUtils.authenticate(
                                    (HttpServletRequest) request, apiTokenRepository));
        } catch (BadCredentialsException e) {
            LOGGER.warn(e.getMessage());
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        }
        chain.doFilter(request, response);
    }
}
