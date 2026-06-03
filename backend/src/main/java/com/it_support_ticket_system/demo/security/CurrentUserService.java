package com.it_support_ticket_system.demo.security;

import com.it_support_ticket_system.demo.common.UnauthorizedException;
import com.it_support_ticket_system.demo.users.AppUser;
import com.it_support_ticket_system.demo.users.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            throw new UnauthorizedException("Authentication is required.");
        }

        return userRepository.findById(securityUser.getId())
            .orElseThrow(() -> new UnauthorizedException("Authentication is required."));
    }
}
