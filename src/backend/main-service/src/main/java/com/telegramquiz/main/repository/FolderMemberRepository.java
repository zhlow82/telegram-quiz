package com.telegramquiz.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.telegramquiz.main.entity.FolderMember;
import com.telegramquiz.main.entity.FolderMemberStatus;

public interface FolderMemberRepository extends JpaRepository<FolderMember, Long> {

    List<FolderMember> findAllByFolderId(Long folderId);

    List<FolderMember> findAllByUsernameAndStatus(String username, FolderMemberStatus status);

    Optional<FolderMember> findByFolderIdAndUsername(Long folderId, String username);

    void deleteAllByFolderId(Long folderId);
}
