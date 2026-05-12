package com.taskmanager.task_manager.repository.logging;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskmanager.task_manager.entity.logging.LoginLog;

import java.util.UUID;

public interface LoginLogRepository extends JpaRepository<LoginLog, UUID> {
}
