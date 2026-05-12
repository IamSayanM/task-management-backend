package com.taskmanager.task_manager.service;

import org.springframework.data.domain.Pageable;

import com.taskmanager.task_manager.dto.request.CreateTaskRequestDto;
import com.taskmanager.task_manager.dto.request.PatchTaskRequestDto;
import com.taskmanager.task_manager.dto.response.TaskResponseDto;
import com.taskmanager.task_manager.enums.TaskPriority;
import com.taskmanager.task_manager.enums.TaskStatus;
import com.taskmanager.task_manager.util.PagedResponse;

public interface TaskService {
    TaskResponseDto createTask(CreateTaskRequestDto request, String userId);

    PagedResponse<TaskResponseDto> getAllMyTasks(String userId, Pageable pageable);

    TaskResponseDto getTaskById(String taskId, String userId);

    PagedResponse<TaskResponseDto> getTasksCreatedByMe(String userId, Pageable pageable);

    PagedResponse<TaskResponseDto> getTasksAssignedToMe(String userId, Pageable pageable);

    PagedResponse<TaskResponseDto> getTasksByStatus(String userId, TaskStatus status, Pageable pageable);

    PagedResponse<TaskResponseDto> getTasksByPriority(String userId, TaskPriority priority, Pageable pageable);

    TaskResponseDto patchTask(String taskId, PatchTaskRequestDto request, String userId);

    void deleteTask(String taskId, String userId);
}
