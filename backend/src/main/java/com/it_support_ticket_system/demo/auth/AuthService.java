package com.it_support_ticket_system.demo.auth;

import com.it_support_ticket_system.demo.common.ConflictException;
import com.it_support_ticket_system.demo.common.UnauthorizedException;
import com.it_support_ticket_system.demo.security.CurrentUserService;
import com.it_support_ticket_system.demo.security.JwtService;
import com.it_support_ticket_system.demo.security.SecurityUser;
import com.it_support_ticket_system.demo.users.AppUser;
import com.it_support_ticket_system.demo.users.Role;
import com.it_support_ticket_system.demo.users.UserRepository;
import com.it_support_ticket_system.demo.users.UserStatus;
import java.util.Locale;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        CurrentUserService currentUserService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public AuthUserResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("User with email '%s' already exists.".formatted(normalizedEmail));
        }

        AppUser user = new AppUser(
            request.name().trim(),
            normalizedEmail,
            passwordEncoder.encode(request.password()),
            Role.USER,
            UserStatus.ACTIVE
        );
        AppUser savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.email().trim().toLowerCase(Locale.ROOT),
                    request.password()
                )
            );
            SecurityUser user = (SecurityUser) authentication.getPrincipal();
            return new LoginResponse(jwtService.generateToken(user));
        } catch (AuthenticationException exception) {
            throw new UnauthorizedException("Invalid email or password.");
        }
    }

    @Transactional(readOnly = true)
    public AuthUserResponse getCurrentUser() {
        return toResponse(currentUserService.getCurrentUser());
    }

    private AuthUserResponse toResponse(AppUser user) {
        return new AuthUserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getStatus());
    }
}
