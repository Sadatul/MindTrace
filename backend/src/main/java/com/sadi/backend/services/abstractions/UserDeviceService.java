package com.sadi.backend.services.abstractions;

import com.sadi.backend.dtos.requests.UserDeviceReq;

public interface UserDeviceService {
    String addUserDevice(UserDeviceReq req, String userId);
    void deleteTokenByDeviceId(String deviceId, String userId);
}
