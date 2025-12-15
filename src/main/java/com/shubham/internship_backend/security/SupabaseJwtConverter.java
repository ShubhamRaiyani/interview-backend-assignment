package com.shubham.internship_backend.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SupabaseJwtConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        String userId = jwt.getSubject(); // Supabase user ID

        Map<String, Object> appMetadata = jwt.getClaim("app_metadata");

        String role = "guest";
        if (appMetadata != null && appMetadata.get("role") != null) {
            role = appMetadata.get("role").toString();
        }

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

        return new JwtAuthenticationToken(jwt, authorities, userId);
    }
}