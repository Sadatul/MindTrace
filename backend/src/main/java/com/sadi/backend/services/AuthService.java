package com.sadi.backend.services;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.sadi.backend.enums.Role;
import com.sadi.backend.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
public class AuthService {
    public boolean userExists(String uuid) {
        return false;
    }

    public void registerUser(Role role){
        String uuid = SecurityUtils.getName();
        if(userExists(uuid)) throw new RuntimeException("User with uuid " + uuid + " already exists");

        addScope(uuid, role);
    }

    public void addScope(String uuid, Role role){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        try {
            firebaseAuth.setCustomUserClaims(uuid, Collections.singletonMap("scp", role.toString()));
        } catch (FirebaseAuthException e) {
            log.error("Error adding scope to user in firebase: {}", e.getMessage());
            throw new InternalError("Error adding scope to user in firebase");
        }
    }
}
