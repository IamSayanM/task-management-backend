package com.taskmanager.task_manager.entity.logging;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_logs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskLog {

    @Id
    @GeneratedValue()
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "performed_by", nullable = false)
    private UUID performedBy;

    @Column(name = "performer_email", nullable = false)
    private String performerEmail;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "task_title", columnDefinition = "TEXT")
    private String taskTitle;

    @Column(name = "assigned_to_email")
    private String assignedToEmail;

    @Column(name = "logged_at", nullable = false)
    private ZonedDateTime loggedAt;

    @PrePersist
    protected void onCreate() {
        this.loggedAt = ZonedDateTime.now();
    }
}
