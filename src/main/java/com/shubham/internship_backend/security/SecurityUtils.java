package com.shubham.internship_backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static String getCurrentUserId() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();
        return auth.getName(); // Supabase sub
    }
}

