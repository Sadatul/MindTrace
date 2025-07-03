package com.sadi.backend.utils;

import lombok.Setter;

public class CodeGenerator {

    @Setter
    private static OtpGenerator delegate = new DefaultOtpGenerator();

    public static String generateOtp() {
        if (delegate == null) {
            throw new IllegalStateException("OtpGenerator not initialized");
        }
        return delegate.generateOtp();
    }
}