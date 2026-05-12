package com.taskmanager.task_manager.dto.request;

import com.taskmanager.task_manager.enums.TaskPriority;
import com.taskmanager.task_manager.enums.TaskStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class PatchTaskRequestDto {

    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title; // null = don't change

    private String description; // null = don't change

    private TaskStatus status; // null = don't change

    private TaskPriority priority; // null = don't change

    private ZonedDateTime dueDate; // null = don't change

    @Email(message = "Assignee email must be valid")
    private String assigneeEmail; // null = don't change

    private UUID categoryId; // null = don't change
}
