package com.taskmanager.task_manager.repository.primary;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskmanager.task_manager.entity.primary.TaskCategory;
import java.util.Optional;

@Repository
public interface TaskCategoryRepository extends JpaRepository<TaskCategory, UUID> {
    Optional<TaskCategory> findByIdAndUserId(UUID id, UUID userId);
}
