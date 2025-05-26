package com.sadi.backend.repositories;

import com.sadi.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Boolean existsByEmail(String email);
}
