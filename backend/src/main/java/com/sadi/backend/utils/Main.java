package com.sadi.backend.utils;

import java.time.Instant;
import java.time.ZoneId;

public class Main {
    public static void main(String[] args) {
        Instant instant = Instant.now();
        System.out.println(instant.atZone(ZoneId.of("Asia/Dhaka")).toInstant());
    }
}
