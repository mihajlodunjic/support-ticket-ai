package com.it_support_ticket_system.demo.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be a valid email address.")
    @Size(max = 255, message = "Email must be at most 255 characters long.")
    String email,
    @NotBlank(message = "Password is required.")
    @Size(max = 255, message = "Password must be at most 255 characters long.")
    String password
) {
}
