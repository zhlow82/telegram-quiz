package com.telegramquiz.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.telegramquiz.main.entity.Folder;
import com.telegramquiz.main.entity.FolderMemberStatus;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findAllByCreatedByOrderBySortOrderAscCreatedAtAsc(String createdBy);
    Optional<Folder> findByIdAndCreatedBy(Long id, String createdBy);

    @Query("SELECT f FROM Folder f WHERE f.id IN " +
           "(SELECT m.folderId FROM FolderMember m WHERE m.username = :username AND m.status = :status) " +
           "ORDER BY f.name ASC")
    List<Folder> findSharedWithUser(@Param("username") String username, @Param("status") FolderMemberStatus status);
}
