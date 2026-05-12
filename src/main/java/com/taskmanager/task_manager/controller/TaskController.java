package com.taskmanager.task_manager.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taskmanager.task_manager.dto.request.CreateTaskRequestDto;
import com.taskmanager.task_manager.dto.request.PatchTaskRequestDto;
import com.taskmanager.task_manager.dto.response.TaskResponseDto;
import com.taskmanager.task_manager.enums.TaskPriority;
import com.taskmanager.task_manager.enums.TaskStatus;
import com.taskmanager.task_manager.service.TaskService;
import com.taskmanager.task_manager.util.ApiResponse;
import com.taskmanager.task_manager.util.PagedResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/createTask")
    public ResponseEntity<ApiResponse<TaskResponseDto>> createTask(@Valid @RequestBody CreateTaskRequestDto request,
            @AuthenticationPrincipal String userId) {
        TaskResponseDto response = taskService.createTask(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Task created successfully", 201));
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponseDto>>> getAllMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal String userId) {

        Pageable pageable = buildPageable(page, size, sortBy, sortDirection);
        PagedResponse<TaskResponseDto> response = taskService.getAllMyTasks(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Tasks fetched successfully", 200));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponseDto>> getTaskById(
            @PathVariable String taskId,
            @AuthenticationPrincipal String userId) {

        TaskResponseDto response = taskService.getTaskById(taskId, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Task fetched successfully", 200));
    }

    @GetMapping("/created-by-me")
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponseDto>>> getTasksCreatedByMe(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal String userId) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDirection);
        PagedResponse<TaskResponseDto> response = taskService.getTasksCreatedByMe(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Tasks fetched successfully", 200));
    }

    @GetMapping("/assigned-to-me")
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponseDto>>> getTasksAssignedToMe(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal String userId) {

        Pageable pageable = buildPageable(page, size, sortBy, sortDirection);
        PagedResponse<TaskResponseDto> response = taskService.getTasksAssignedToMe(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Tasks fetched successfully", 200));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponseDto>>> getTasksByStatus(
            @PathVariable TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal String userId) {

        Pageable pageable = buildPageable(page, size, sortBy, sortDirection);
        PagedResponse<TaskResponseDto> response = taskService.getTasksByStatus(userId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Tasks fetched successfully", 200));
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponseDto>>> getTasksByPriority(
            @PathVariable TaskPriority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal String userId) {

        Pageable pageable = buildPageable(page, size, sortBy, sortDirection);
        PagedResponse<TaskResponseDto> response = taskService.getTasksByPriority(userId, priority, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Tasks fetched successfully", 200));
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponseDto>> patchTask(
            @PathVariable String taskId,
            @Valid @RequestBody PatchTaskRequestDto request,
            @AuthenticationPrincipal String userId) {
        TaskResponseDto response = taskService.patchTask(taskId, request, userId);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Task updated successfully", 200));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable String taskId,
            @AuthenticationPrincipal String userId) {
        taskService.deleteTask(taskId, userId);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Task deleted successfully", 200));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDirection) {
        int safeSize = Math.min(size, 50);
        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return PageRequest.of(page, safeSize, sort);
    }
}
