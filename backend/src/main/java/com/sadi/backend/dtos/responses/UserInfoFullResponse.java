package com.sadi.backend.dtos.responses;

import com.sadi.backend.enums.Gender;
import com.sadi.backend.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoFullResponse {
    private String id;
    private String name;
    private String email;
    private Role role;
    private Gender gender;
    private LocalDate dob;
    private String profilePicture;
    private CaregiversPatientsDTO primaryContact;
    private Instant createdAt;
    private String telegramChatId;
}
