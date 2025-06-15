package com.sadi.backend.controllers;

import com.sadi.backend.dtos.responses.CaregiversPatientsDTO;
import com.sadi.backend.dtos.responses.UserInfoFullResponse;
import com.sadi.backend.entities.PatientDetail;
import com.sadi.backend.entities.User;
import com.sadi.backend.enums.Role;
import com.sadi.backend.services.UserService;
import com.sadi.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserInfoController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserInfoFullResponse> getUserInfo(
            @RequestParam(required = false) String userId
    ) {
        // This method will be implemented to return user information
        if(userId == null) {
            userId = SecurityUtils.getName();
        }
        log.debug("Received request to get user info");
        User user = userService.getUser(userId);
        CaregiversPatientsDTO dto = null;
        if(user.getRole() == Role.PATIENT)
        {
            PatientDetail patientDetail = userService.getPatientDetail(userId, false);
            User primaryContact = patientDetail.getPrimaryContact();
            dto = new CaregiversPatientsDTO(primaryContact.getId(), primaryContact.getName(),
                    primaryContact.getGender(), primaryContact.getProfilePicture(), null);
        }

        UserInfoFullResponse response = new UserInfoFullResponse(user.getId(), user.getName(), user.getEmail(),
                user.getRole(), user.getGender(), user.getDateOfBirth(), user.getProfilePicture(),
                dto, user.getCreatedAt(), user.getTelegramChatId());
        return ResponseEntity.ok(response);
    }

}
