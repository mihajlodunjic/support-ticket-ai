package com.it_support_ticket_system.demo.common;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommonExceptionTestController {

    @PostMapping("/api/test/validation")
    public void validate(@Valid @RequestBody TestRequest request) {
    }

    @GetMapping("/api/test/not-found")
    public void notFound() {
        throw new ResourceNotFoundException("Ticket with id 99 was not found.");
    }

    @GetMapping("/api/test/bad-request")
    public void badRequest() {
        throw new BadRequestException("Request payload is invalid.");
    }

    @GetMapping("/api/test/conflict")
    public void conflict() {
        throw new ConflictException("Category already exists.");
    }

    public record TestRequest(@NotBlank(message = "Name is required.") String name) {
    }
}
