package com.shubham.internship_backend.config;

import com.shubham.internship_backend.security.SupabaseJwtAuthenticationConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Value("${spring.security.secret}")
        private String jwtSecret;

        private final SupabaseJwtAuthenticationConverter supabaseJwtAuthenticationConverter;

        public SecurityConfig(SupabaseJwtAuthenticationConverter supabaseJwtAuthenticationConverter) {
                this.supabaseJwtAuthenticationConverter = supabaseJwtAuthenticationConverter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // Public Endpoints (Actuator)
                                                .requestMatchers("/actuator/**").permitAll()
                                                // Secured Endpoints
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt
                                                                .decoder(jwtDecoder())
                                                                .jwtAuthenticationConverter(
                                                                                supabaseJwtAuthenticationConverter)));

                return http.build();
        }

        @Bean
        public JwtDecoder jwtDecoder() {
                SecretKey key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HMACSHA256");
                return NimbusJwtDecoder.withSecretKey(key)
                                .macAlgorithm(MacAlgorithm.HS256)
                                .build();
        }
}
