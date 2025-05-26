package com.sadi.backend.services.abstractions;

public interface UserVerificationService {
    String cacheOtp();
    Boolean verifyOtp(String userId, String otp);
}
