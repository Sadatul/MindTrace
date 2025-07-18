package com.sadi.backend.services.impls;

import com.sadi.backend.dtos.requests.UserDeviceReq;
import com.sadi.backend.entities.User;
import com.sadi.backend.entities.UserDevice;
import com.sadi.backend.repositories.UserDeviceRepository;
import com.sadi.backend.services.UserService;
import com.sadi.backend.services.abstractions.UserDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserDeviceServiceImpl implements UserDeviceService {
    private final UserService userService;
    private final UserDeviceRepository userDeviceRepository;

    @Override
    public String addUserDevice(UserDeviceReq req, String userId) {
        User user = userService.getUser(userId);
        UserDevice userDevice = new UserDevice(req.deviceId(), user, req.token(), req.deviceName());
        userDeviceRepository.save(userDevice);
        return req.deviceId();
    }

    @Override
    public void deleteTokenByDeviceId(String deviceId, String userId) {
        userDeviceRepository.findById(deviceId).ifPresent(
                userDevice -> {
                    verifyUserDevice(userDevice, userId);
                    userDeviceRepository.delete(userDevice);
                }
        );
    }

    private void verifyUserDevice(UserDevice userDevice, String userId) {
        if (!userDevice.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to perform this operation");
        }
    }
}
