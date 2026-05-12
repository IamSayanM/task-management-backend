package com.taskmanager.task_manager.entity.logging;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "login_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginLog {

    @Id
    @GeneratedValue
    private UUID id;

    private String email;

    private boolean success;

    private String ipAddress;

    private ZonedDateTime loginTime;
}
