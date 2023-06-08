package com.hierarchy.gmbh.api.employee.relationship.security.data;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class HierarchyGmbHApiAuthenticationObject extends AbstractAuthenticationToken {
    private final String token;

    public HierarchyGmbHApiAuthenticationObject(
            Collection<? extends GrantedAuthority> authorities, String token) {
        super(authorities);
        setAuthenticated(true);
        this.token = token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }
}
