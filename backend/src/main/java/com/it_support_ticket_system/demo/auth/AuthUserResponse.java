package com.it_support_ticket_system.demo.auth;

import com.it_support_ticket_system.demo.users.Role;
import com.it_support_ticket_system.demo.users.UserStatus;

public record AuthUserResponse(
    Long id,
    String name,
    String email,
    Role role,
    UserStatus status
) {
}
