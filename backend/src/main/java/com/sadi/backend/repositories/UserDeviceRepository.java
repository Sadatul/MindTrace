package com.sadi.backend.repositories;

import com.sadi.backend.entities.User;
import com.sadi.backend.entities.UserDevice;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, String> {
    List<UserDevice> findByUser(User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserDevice ud where ud.token in :tokens and ud.user.id = :userId")
    void deleteAllTokens(List<String> tokens, String userId);
}
