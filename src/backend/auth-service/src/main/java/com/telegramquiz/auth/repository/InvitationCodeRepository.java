package com.telegramquiz.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.telegramquiz.auth.model.InvitationCode;

public interface InvitationCodeRepository extends JpaRepository<InvitationCode, Long> {
    Optional<InvitationCode> findByCodeAndActiveTrue(String code);
    List<InvitationCode> findAllByOrderByCreatedAtDesc();
}
