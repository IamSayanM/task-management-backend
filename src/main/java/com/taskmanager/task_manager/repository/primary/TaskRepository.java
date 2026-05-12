package com.taskmanager.task_manager.repository.primary;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.taskmanager.task_manager.entity.primary.Task;
import com.taskmanager.task_manager.enums.TaskPriority;
import com.taskmanager.task_manager.enums.TaskStatus;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("SELECT t FROM Task t WHERE t.creator.id = :userId OR t.assignee.id = :userId")
    Page<Task> findAllTasksForUser(@Param("userId") UUID userId, Pageable pageable);

    Page<Task> findByCreatorId(UUID creatorId, Pageable pageable);

    Page<Task> findByAssigneeId(UUID assigneeId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.creator.id = :userId OR t.assignee.id = :userId AND t.status = :status")
    Page<Task> findByUserAndStatus(@Param("userId") UUID userId,
            @Param("status") TaskStatus status,
            Pageable pageable);

    @Query("SELECT t FROM Task t WHERE (t.creator.id = :userId OR t.assignee.id = :userId) AND t.priority = :priority")
    Page<Task> findByUserAndPriority(@Param("userId") UUID userId,
            @Param("priority") TaskPriority priority,
            Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.id = :taskId AND (t.creator.id = :userId OR t.assignee.id = :userId)")
    Optional<Task> findByIdAndUser(@Param("taskId") UUID taskId,
            @Param("userId") UUID userId);
}
