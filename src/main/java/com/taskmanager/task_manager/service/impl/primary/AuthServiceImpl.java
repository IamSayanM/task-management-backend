package com.taskmanager.task_manager.service.impl.primary;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.taskmanager.task_manager.dto.request.LoginRequestDto;
import com.taskmanager.task_manager.dto.response.AuthResponseDto;
import com.taskmanager.task_manager.entity.primary.User;
import com.taskmanager.task_manager.exception.AccountDisabledException;
import com.taskmanager.task_manager.exception.AccountLockedException;
import com.taskmanager.task_manager.exception.InvalidCredentialsException;
import com.taskmanager.task_manager.repository.logging.LoginLogRepository;
import com.taskmanager.task_manager.repository.primary.UserRepository;
import com.taskmanager.task_manager.security.JwtTokenProvider;
import com.taskmanager.task_manager.service.AuthService;
import com.taskmanager.task_manager.service.impl.logging.LoggingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginLogRepository loginLogRepository;
    private final LoggingService loggingService;

    @Value("${app.security.max-login-attempts}")
    private int maxLoginAttempts;

    @Override
    @Transactional
    public AuthResponseDto login(LoginRequestDto request) {
        User user = null;
        boolean success = false;
        try {
            user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                    .orElseThrow(InvalidCredentialsException::new);
            if (!user.isEnabled()) {
                throw new AccountDisabledException();
            }
            if (user.isLocked()) {
                throw new AccountLockedException();
            }
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                handleFailedAttempt(user);
                throw new InvalidCredentialsException();
            }

            userRepository.resetFailedAttempts(user.getEmail());

            String accessToken = jwtTokenProvider.generateAccessToken(
                    user.getId().toString(), user.getEmail(), user.getRole());

            String refreshToken = jwtTokenProvider.generateRefreshToken(
                    user.getId().toString());

            success = true;

            log.info("Successful login for user: {}", user.getId());

            return AuthResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getAccessTokenExpiryMs() / 1000)
                    .user(AuthResponseDto.UserInfoDto.builder()
                            .id(user.getId().toString())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .role(user.getRole())
                            .build())
                    .build();
        } catch (Exception ex) {
            log.warn("Login failed for email: {}", request.getEmail());

            throw ex;
        } finally {
            try {
                loggingService.saveLoginLog(request.getEmail(), success);
            } catch (Exception logEx) {
                log.error("Failed to save login log", logEx);
            }
        }

    }

    private void handleFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        if (attempts >= maxLoginAttempts) {
            userRepository.lockAccount(user.getEmail());
            log.warn("Account locked after {} failed attempts: {}", attempts, user.getId());
        } else {
            userRepository.incrementFailedAttempts(user.getEmail());
        }
    }

}
