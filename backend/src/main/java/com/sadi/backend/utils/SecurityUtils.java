package com.sadi.backend.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class SecurityUtils {
    public static String getName(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public static Jwt getPrinciple(){
        return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
