package com.taskmanager.task_manager.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("No account found for email: " + email);
    }
}
