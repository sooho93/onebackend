package com.example.onepointup.repository;

import com.example.onepointup.model.Role;
import com.example.onepointup.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    @Modifying
    @Query("update User set role=:role where  username=:username")
    void updateUserRole(@Param("username")String username, @Param("role") Role role);

    @Modifying
    @Query("UPDATE User u SET u.profileImage = :profileImage WHERE u.username = :username")
    void updateUserProfileImage(@Param("username") String username, @Param("profileImage") byte[] profileImage);

    Optional<User> findById(Long id);
}
