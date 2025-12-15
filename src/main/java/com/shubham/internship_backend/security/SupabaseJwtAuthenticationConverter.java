package com.shubham.internship_backend.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class SupabaseJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities, extractPrincipal(jwt));
    }

    private String extractPrincipal(Jwt jwt) {
        return jwt.getSubject();
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Map<String, Object> appMetadata = jwt.getClaim("app_metadata");
        if (appMetadata == null) {
            return Collections.emptyList();
        }

        // Handle generic 'role' or 'roles' in app_metadata if present
        // Expected format: app_metadata: { role: "staff" } or roles: ["staff",
        // "reception"]
        // Also handling 'supabase_role' which is usually just 'authenticated'

        // Strategy: Look for specific custom role claims first
        if (appMetadata.containsKey("role")) {
            Object roleObj = appMetadata.get("role");
            if (roleObj instanceof String) {
                return List.of(new SimpleGrantedAuthority("ROLE_" + ((String) roleObj).toUpperCase()));
            }
        }

        // Fallback to Supabase default role
        String supabaseRole = (String) appMetadata.get("supabase_role"); // e.g., "authenticated"
        if (supabaseRole != null) {
            return List.of(new SimpleGrantedAuthority("ROLE_" + supabaseRole.toUpperCase()));
        }

        return Collections.emptyList();
    }
}
