package com.taskmanager.task_manager.service;

import org.springframework.stereotype.Service;

import com.taskmanager.task_manager.dto.request.LoginRequestDto;
import com.taskmanager.task_manager.dto.response.AuthResponseDto;

public interface AuthService {
    AuthResponseDto login(LoginRequestDto request);
}
