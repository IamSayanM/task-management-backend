package com.taskmanager.task_manager.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserInfoDto user;

    @Data
    @Builder
    public static class UserInfoDto {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
    }
}
