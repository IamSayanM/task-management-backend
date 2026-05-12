package com.taskmanager.task_manager.dto.request;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.taskmanager.task_manager.enums.TaskPriority;
import com.taskmanager.task_manager.enums.TaskStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTaskRequestDto {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private ZonedDateTime dueDate;

    @Email(message = "Assignee email must be a valid email address")
    private String assigneeEmail;

    private UUID categoryId;
}
