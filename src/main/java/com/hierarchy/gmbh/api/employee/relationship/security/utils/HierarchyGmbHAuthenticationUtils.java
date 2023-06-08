package com.hierarchy.gmbh.api.employee.relationship.security.utils;

import com.hierarchy.gmbh.api.employee.relationship.jpa.repository.ApiTokenEntityRepository;
import com.hierarchy.gmbh.api.employee.relationship.security.data.HierarchyGmbHApiAuthenticationObject;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import javax.servlet.http.HttpServletRequest;

public class HierarchyGmbHAuthenticationUtils {
    private static final String AUTH_TOKEN_HEADER_NAME = "X-API-KEY";

    public static Authentication authenticate(
            HttpServletRequest httpServletRequest, ApiTokenEntityRepository apiTokenRepository) {
        String userApiToken = httpServletRequest.getHeader(AUTH_TOKEN_HEADER_NAME);

        if (userApiToken == null || userApiToken.isEmpty()) {
            throw new BadCredentialsException("There is no credential in the request");
        }
        userApiToken = userApiToken.trim();

        if (apiTokenRepository.existsById(userApiToken)) {
            return new HierarchyGmbHApiAuthenticationObject(
                    AuthorityUtils.NO_AUTHORITIES, userApiToken);
        }
        throw new BadCredentialsException("Token doesn't exist");
    }
}
