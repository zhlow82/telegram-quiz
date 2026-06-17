package com.telegramquiz.main.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.telegramquiz.main.entity.FolderMemberStatus;
import com.telegramquiz.main.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByOrderByOrderIndexAsc();
    List<Question> findAllByCreatedByOrderByOrderIndexAsc(String createdBy);
    Optional<Question> findByIdAndCreatedBy(Long id, String createdBy);
    boolean existsByIdAndCreatedBy(Long id, String createdBy);
    List<Question> findAllByIdInAndCreatedBy(List<Long> ids, String createdBy);
    long countByCreatedBy(String createdBy);
    List<Question> findAllByCreatedByAndFolderIdOrderByOrderIndexAsc(String createdBy, Long folderId);
    List<Question> findAllByFolderIdOrderByOrderIndexAsc(Long folderId);

    @Query("SELECT MAX(q.orderIndex) FROM Question q WHERE q.createdBy = :username")
    Optional<Integer> findMaxOrderIndexByCreatedBy(@Param("username") String username);

    @Query("SELECT q FROM Question q WHERE " +
           "(q.createdBy = :username OR " +
           "(q.folderId IS NOT NULL AND q.folderId IN " +
           "(SELECT m.folderId FROM FolderMember m WHERE m.username = :username AND m.status = :status))) " +
           "ORDER BY q.orderIndex ASC")
    List<Question> findAllAccessible(@Param("username") String username, @Param("status") FolderMemberStatus status);

    @Query("SELECT q FROM Question q WHERE q.id IN :ids AND " +
           "(q.createdBy = :username OR " +
           "(q.folderId IS NOT NULL AND q.folderId IN " +
           "(SELECT m.folderId FROM FolderMember m WHERE m.username = :username AND m.status = :status)))")
    List<Question> findAllAccessibleByIds(@Param("ids") List<Long> ids, @Param("username") String username, @Param("status") FolderMemberStatus status);
}
