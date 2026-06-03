package com.it_support_ticket_system.demo.security;

import com.it_support_ticket_system.demo.users.AppUser;
import com.it_support_ticket_system.demo.users.Role;
import com.it_support_ticket_system.demo.users.UserRepository;
import com.it_support_ticket_system.demo.users.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;

    public AdminUserSeeder(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        SecurityProperties securityProperties
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityProperties = securityProperties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        SecurityProperties.Admin admin = securityProperties.getAdmin();
        if (!hasText(admin.getName()) || !hasText(admin.getEmail()) || !hasText(admin.getPassword())) {
            return;
        }

        String normalizedEmail = admin.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return;
        }

        AppUser adminUser = new AppUser(
            admin.getName().trim(),
            normalizedEmail,
            passwordEncoder.encode(admin.getPassword()),
            Role.ADMIN,
            UserStatus.ACTIVE
        );
        userRepository.save(adminUser);
        log.info("Seeded admin user '{}'.", normalizedEmail);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
