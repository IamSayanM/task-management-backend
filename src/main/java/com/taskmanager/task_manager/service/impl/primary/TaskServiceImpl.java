package com.taskmanager.task_manager.service.impl.primary;

import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.taskmanager.task_manager.dto.request.CreateTaskRequestDto;
import com.taskmanager.task_manager.dto.request.PatchTaskRequestDto;
import com.taskmanager.task_manager.dto.response.TaskResponseDto;
import com.taskmanager.task_manager.entity.primary.Task;
import com.taskmanager.task_manager.entity.primary.TaskCategory;
import com.taskmanager.task_manager.entity.primary.User;
import com.taskmanager.task_manager.enums.TaskLogAction;
import com.taskmanager.task_manager.enums.TaskPriority;
import com.taskmanager.task_manager.enums.TaskStatus;
import com.taskmanager.task_manager.exception.ResourceNotFoundException;
import com.taskmanager.task_manager.repository.primary.TaskCategoryRepository;
import com.taskmanager.task_manager.repository.primary.TaskRepository;
import com.taskmanager.task_manager.repository.primary.UserRepository;
import com.taskmanager.task_manager.service.TaskService;
import com.taskmanager.task_manager.service.impl.logging.LoggingService;
import com.taskmanager.task_manager.util.PagedResponse;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

        private final TaskRepository taskRepository;
        private final UserRepository userRepository;
        private final TaskCategoryRepository taskCategoryRepository;
        private final LoggingService loggingService;

        @Override
        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "allTasksForUser", key = "#userId"),
                        @CacheEvict(value = "tasksByStatus", allEntries = true),
                        @CacheEvict(value = "tasksByPriority", allEntries = true),
                        @CacheEvict(value = "tasksCreatedByMe", key = "#userId"),
                        @CacheEvict(value = "tasksAssignedToMe", key = "#userId")
        })
        public TaskResponseDto createTask(CreateTaskRequestDto request, String userId) {
                // --get logged in user/ creator
                //
                User creator = userRepository.findById(UUID.fromString(userId))
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                //
                // --get the asignee
                //
                User assignee = null;
                if (request.getAssigneeEmail() != null && !request.getAssigneeEmail().isBlank()) {
                        assignee = userRepository.findByEmail(request.getAssigneeEmail().toLowerCase().trim())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "No user found with email: " + request.getAssigneeEmail()));
                }
                //
                // --Get category if provided
                //

                TaskCategory category = null;
                if (request.getCategoryId() != null) {
                        category = taskCategoryRepository
                                        .findByIdAndUserId(request.getCategoryId(), UUID.fromString(userId))
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Category not found with id: " + request.getCategoryId()));
                }

                Task task = Task.builder()
                                .title(request.getTitle())
                                .description(request.getDescription())
                                .status(request.getStatus()) // @PrePersist defaults to TODO if null
                                .priority(request.getPriority()) // @PrePersist defaults to MEDIUM if null
                                .dueDate(request.getDueDate())
                                .creator(creator)
                                .assignee(assignee)
                                .category(category)
                                .build();

                Task savedTask = taskRepository.save(task);
                loggingService.saveTaskLog(
                                savedTask.getId(),
                                savedTask.getTitle(),
                                UUID.fromString(userId),
                                creator.getEmail(),
                                TaskLogAction.CREATED,
                                null, // no old value for creation
                                savedTask.getTitle(),
                                assignee != null ? assignee.getEmail() : null);
                log.info("Task created with id: {} by user: {}", savedTask.getId(), userId);
                return mapToResponseDto(savedTask);
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "allTasksForUser", key = "#userId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
        public PagedResponse<TaskResponseDto> getAllMyTasks(String userId, Pageable pageable) {
                Page<Task> tasks = taskRepository.findAllTasksForUser(UUID.fromString(userId), pageable);
                return PagedResponse.of(tasks.map(this::mapToResponseDto));
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "taskById", key = "#taskId")
        public TaskResponseDto getTaskById(String taskId, String userId) {
                Task task = taskRepository.findByIdAndUser(UUID.fromString(taskId), UUID.fromString(userId))
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Task not found with id: " + taskId));
                return mapToResponseDto(task);
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "tasksCreatedByMe", key = "#userId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
        public PagedResponse<TaskResponseDto> getTasksCreatedByMe(String userId, Pageable pageable) {
                Page<Task> tasks = taskRepository.findByCreatorId(UUID.fromString(userId), pageable);
                return PagedResponse.of(tasks.map(this::mapToResponseDto));
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "tasksAssignedToMe", key = "#userId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
        public PagedResponse<TaskResponseDto> getTasksAssignedToMe(String userId, Pageable pageable) {
                Page<Task> tasks = taskRepository.findByAssigneeId(
                                UUID.fromString(userId), pageable);
                return PagedResponse.of(tasks.map(this::mapToResponseDto));
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "tasksByStatus", key = "#userId + '_' + #status + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
        public PagedResponse<TaskResponseDto> getTasksByStatus(
                        String userId, TaskStatus status, Pageable pageable) {
                Page<Task> tasks = taskRepository.findByUserAndStatus(
                                UUID.fromString(userId), status, pageable);
                return PagedResponse.of(tasks.map(this::mapToResponseDto));
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "tasksByPriority", key = "#userId + '_' + #priority + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
        public PagedResponse<TaskResponseDto> getTasksByPriority(
                        String userId, TaskPriority priority, Pageable pageable) {
                Page<Task> tasks = taskRepository.findByUserAndPriority(
                                UUID.fromString(userId), priority, pageable);
                return PagedResponse.of(tasks.map(this::mapToResponseDto));
        }

        @Override
        @Transactional
        @Caching(put = { @CachePut(value = "taskById", key = "#taskId") }, evict = {
                        @CacheEvict(value = "allTasksForUser", allEntries = true),
                        @CacheEvict(value = "tasksByStatus", allEntries = true),
                        @CacheEvict(value = "tasksByPriority", allEntries = true),
                        @CacheEvict(value = "tasksCreatedByMe", allEntries = true),
                        @CacheEvict(value = "tasksAssignedToMe", allEntries = true)
        })
        public TaskResponseDto patchTask(
                        String taskId,
                        PatchTaskRequestDto request,
                        String userId) {

                Task task = taskRepository.findByIdAndUser(
                                UUID.fromString(taskId), UUID.fromString(userId))
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Task not found with id: " + taskId));

                User performer = userRepository.findById(UUID.fromString(userId))
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (request.getTitle() != null) {
                        task.setTitle(request.getTitle());
                        logChange(task, performer, TaskLogAction.UPDATED,
                                        "title", task.getTitle(), request.getTitle());
                }

                if (request.getDescription() != null) {
                        task.setDescription(request.getDescription());
                }

                if (request.getStatus() != null && !request.getStatus().equals(task.getStatus())) {
                        String oldStatus = task.getStatus().name();
                        task.setStatus(request.getStatus());
                        logChange(task, performer, TaskLogAction.STATUS_CHANGED,
                                        oldStatus, request.getStatus().name(), null);
                }

                if (request.getPriority() != null && !request.getPriority().equals(task.getPriority())) {
                        String oldPriority = task.getPriority().name();
                        task.setPriority(request.getPriority());
                        logChange(task, performer, TaskLogAction.PRIORITY_CHANGED,
                                        oldPriority, request.getPriority().name(), null);
                }

                if (request.getDueDate() != null) {
                        task.setDueDate(request.getDueDate());
                }

                if (request.getAssigneeEmail() != null) {
                        if (request.getAssigneeEmail().isBlank()) {
                                // Empty string = remove assignee
                                task.setAssignee(null);
                                logChange(task, performer, TaskLogAction.ASSIGNED,
                                                task.getAssignee() != null ? task.getAssignee().getEmail() : null,
                                                null, null);
                        } else {
                                User newAssignee = userRepository
                                                .findByEmail(request.getAssigneeEmail().toLowerCase().trim())
                                                .orElseThrow(() -> new ResourceNotFoundException(
                                                                "No user found with email: "
                                                                                + request.getAssigneeEmail()));
                                task.setAssignee(newAssignee);
                                logChange(task, performer, TaskLogAction.ASSIGNED,
                                                null, null, newAssignee.getEmail());
                        }
                }

                if (request.getCategoryId() != null) {
                        TaskCategory category = taskCategoryRepository
                                        .findByIdAndUserId(request.getCategoryId(), UUID.fromString(userId))
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Category not found with id: " + request.getCategoryId()));
                        task.setCategory(category);
                }

                Task updatedTask = taskRepository.save(task);
                log.info("Task patched: {} by user: {}", taskId, userId);
                return mapToResponseDto(updatedTask);
        }

        private TaskResponseDto mapToResponseDto(Task task) {
                return TaskResponseDto.builder()
                                .id(task.getId())
                                .title(task.getTitle())
                                .description(task.getDescription())
                                .status(task.getStatus())
                                .priority(task.getPriority())
                                .dueDate(task.getDueDate())
                                .creator(TaskResponseDto.UserSummaryDto.builder()
                                                .id(task.getCreator().getId())
                                                .email(task.getCreator().getEmail())
                                                .firstName(task.getCreator().getFirstName())
                                                .lastName(task.getCreator().getLastName())
                                                .build())
                                .assignee(task.getAssignee() != null ? TaskResponseDto.UserSummaryDto.builder()
                                                .id(task.getAssignee().getId())
                                                .email(task.getAssignee().getEmail())
                                                .firstName(task.getAssignee().getFirstName())
                                                .lastName(task.getAssignee().getLastName())
                                                .build() : null)
                                .category(task.getCategory() != null ? TaskResponseDto.CategorySummaryDto.builder()
                                                .id(task.getCategory().getId())
                                                .name(task.getCategory().getName())
                                                .color(task.getCategory().getColor())
                                                .build() : null)
                                .createdAt(task.getCreatedAt())
                                .updatedAt(task.getUpdatedAt())
                                .build();
        }

        @Override
        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "taskById", key = "#taskId"),
                        @CacheEvict(value = "allTasksForUser", allEntries = true),
                        @CacheEvict(value = "tasksByStatus", allEntries = true),
                        @CacheEvict(value = "tasksByPriority", allEntries = true),
                        @CacheEvict(value = "tasksCreatedByMe", allEntries = true),
                        @CacheEvict(value = "tasksAssignedToMe", allEntries = true)
        })
        public void deleteTask(String taskId, String userId) {
                Task task = taskRepository.findByIdAndUser(UUID.fromString(taskId), UUID.fromString(userId))
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Task not found with id: " + taskId));

                User performer = userRepository.findById(UUID.fromString(userId))
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                loggingService.saveTaskLog(
                                task.getId(),
                                task.getTitle(),
                                UUID.fromString(userId),
                                performer.getEmail(),
                                TaskLogAction.DELETED,
                                task.getTitle(),
                                null,
                                null);

                taskRepository.delete(task);
                log.info("Task deleted: {} by user: {}", taskId, userId);
        }

        private void logChange(Task task, User performer, TaskLogAction action,
                        String oldValue, String newValue, String assignedToEmail) {
                loggingService.saveTaskLog(
                                task.getId(),
                                task.getTitle(),
                                performer.getId(),
                                performer.getEmail(),
                                action,
                                oldValue,
                                newValue,
                                assignedToEmail);
        }
}
