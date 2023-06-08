package com.hierarchy.gmbh.api.employee.relationship.security.filter;

import com.hierarchy.gmbh.api.employee.relationship.jpa.repository.ApiTokenEntityRepository;
import com.hierarchy.gmbh.api.employee.relationship.security.utils.HierarchyGmbHAuthenticationUtils;

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
    private final ApiTokenEntityRepository apiTokenEntityRepository;

    public HierarchyGmbHAuthenticationFilter(ApiTokenEntityRepository apiTokenEntityRepository) {
        this.apiTokenEntityRepository = apiTokenEntityRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            SecurityContextHolder.getContext()
                    .setAuthentication(
                            HierarchyGmbHAuthenticationUtils.authenticate(
                                    (HttpServletRequest) request, apiTokenEntityRepository));
        } catch (BadCredentialsException e) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpResponse.getWriter().print("'" + e.getMessage() + "'");
        }
        chain.doFilter(request, response);
    }
}
