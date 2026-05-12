package com.taskmanager.task_manager.service.impl.logging;

import java.time.ZonedDateTime;
import java.util.UUID;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.taskmanager.task_manager.entity.logging.LoginLog;
import com.taskmanager.task_manager.entity.logging.TaskLog;
import com.taskmanager.task_manager.enums.TaskLogAction;
import com.taskmanager.task_manager.repository.logging.LoginLogRepository;
import com.taskmanager.task_manager.repository.logging.TaskLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoggingService {

    private final LoginLogRepository loginLogRepository;
    private final TaskLogRepository taskLogRepository;

    @Async
    @Transactional(transactionManager = "loggingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void saveLoginLog(String email, boolean success) {
        loginLogRepository.save(
                LoginLog.builder()
                        .email(email)
                        .success(success)
                        .loginTime(ZonedDateTime.now())
                        .build());
    }

    public void saveTaskLog(UUID taskId,
            String taskTitle,
            UUID performedBy,
            String performerEmail,
            TaskLogAction action,
            String oldValue,
            String newValue,
            String assignedToEmail) {
        taskLogRepository.save(
                TaskLog.builder()
                        .taskId(taskId)
                        .taskTitle(taskTitle)
                        .performedBy(performedBy)
                        .performerEmail(performerEmail)
                        .action(action.name())
                        .oldValue(oldValue)
                        .newValue(newValue)
                        .assignedToEmail(assignedToEmail)
                        .build());
    }
}