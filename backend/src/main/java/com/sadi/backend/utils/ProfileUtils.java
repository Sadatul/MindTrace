package com.sadi.backend.utils;

import org.springframework.core.env.Environment;

import java.util.Arrays;

public class ProfileUtils {
    public static boolean isTest(Environment env) {
        return Arrays.asList(env.getActiveProfiles()).contains("test");
    }
}