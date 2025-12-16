package com.shubham.internship_backend.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SupabaseJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String userId = jwt.getSubject();
        List<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities, userId);
    }

    private List<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        Map<String, Object> appMetadata = jwt.getClaim("app_metadata");

        if (appMetadata != null && appMetadata.get("role") != null) {
            String role = appMetadata.get("role").toString().trim().toUpperCase();

            // Only allow valid roles
            if (isValidRole(role)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            } else {
                // Invalid role, add minimal permission
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
        } else {
            // No role found, add minimal permission
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return authorities;
    }

    private boolean isValidRole(String role) {
        return role.equals("STAFF") || role.equals("RECEPTION") || role.equals("ADMIN");
    }
}