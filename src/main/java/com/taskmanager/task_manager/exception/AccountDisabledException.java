package com.taskmanager.task_manager.exception;

public class AccountDisabledException extends RuntimeException {
    public AccountDisabledException() {
        super("Account has been disabled. Please contact support.");
    }
}
