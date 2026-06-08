package com.telegramquiz.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.telegramquiz.auth.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByGoogleSub(String googleSub);
    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.active = true AND u.username <> :excludeUsername " +
           "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(COALESCE(u.firstName, '')) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(COALESCE(u.lastName, '')) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<User> searchUsers(@Param("q") String q, @Param("excludeUsername") String excludeUsername, Pageable pageable);
}
