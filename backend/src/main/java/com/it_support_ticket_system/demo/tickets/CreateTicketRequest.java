package com.it_support_ticket_system.demo.tickets;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
    @NotBlank(message = "Title is required.")
    @Size(max = 200, message = "Title must be at most 200 characters long.")
    String title,

    @NotBlank(message = "Description is required.")
    @Size(min = 5, max = 5000, message = "Description must be between 5 and 5000 characters long.")
    String description,

    @NotBlank(message = "User email is required.")
    @Email(message = "User email must be a valid email address.")
    @Size(max = 255, message = "User email must be at most 255 characters long.")
    String userEmail,

    @Size(max = 2000, message = "Notes must be at most 2000 characters long.")
    String notes
) {
}
