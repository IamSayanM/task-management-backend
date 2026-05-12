package com.taskmanager.task_manager.repository.logging;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskmanager.task_manager.entity.logging.TaskLog;

@Repository
public interface TaskLogRepository extends JpaRepository<TaskLog, UUID> {
    // Get all logs for a specific task
    Page<TaskLog> findByTaskIdOrderByLoggedAtDesc(UUID taskId, Pageable pageable);

    // Get all logs performed by a specific user
    Page<TaskLog> findByPerformedByOrderByLoggedAtDesc(UUID performedBy, Pageable pageable);

    // Get all logs for a specific action type
    Page<TaskLog> findByActionOrderByLoggedAtDesc(String action, Pageable pageable);
}
