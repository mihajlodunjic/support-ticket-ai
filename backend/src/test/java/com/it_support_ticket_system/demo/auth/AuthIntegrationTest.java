package com.it_support_ticket_system.demo.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.it_support_ticket_system.demo.users.AppUser;
import com.it_support_ticket_system.demo.users.Role;
import com.it_support_ticket_system.demo.users.UserRepository;
import com.it_support_ticket_system.demo.users.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PASSWORD = "AdminPass123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerCreatesUserWithHashedPassword() throws Exception {
        String email = "phase9-register@example.com";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Phase Nine User",
                      "email": "phase9-register@example.com",
                      "password": "SecretPass123!"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Phase Nine User"))
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));

        AppUser savedUser = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("SecretPass123!");
        assertThat(passwordEncoder.matches("SecretPass123!", savedUser.getPasswordHash())).isTrue();
    }

    @Test
    void duplicateEmailFails() throws Exception {
        String email = "phase9-duplicate@example.com";
        registerUser("Duplicate User", email, "SecretPass123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Another User",
                      "email": "phase9-duplicate@example.com",
                      "password": "SecretPass123!"
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("User with email 'phase9-duplicate@example.com' already exists."));
    }

    @Test
    void loginReturnsJwt() throws Exception {
        registerUser("Login User", "phase9-login@example.com", "SecretPass123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "phase9-login@example.com",
                      "password": "SecretPass123!"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isString())
            .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void invalidLoginFails() throws Exception {
        registerUser("Invalid Login User", "phase9-invalid-login@example.com", "SecretPass123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "phase9-invalid-login@example.com",
                      "password": "WrongPass123!"
                    }
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void authMeReturnsCurrentUser() throws Exception {
        registerUser("Current User", "phase9-me@example.com", "SecretPass123!");
        String token = loginAndExtractToken("phase9-me@example.com", "SecretPass123!");

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Current User"))
            .andExpect(jsonPath("$.email").value("phase9-me@example.com"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void anonymousUserCannotAccessProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/tickets"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    @Test
    void userCannotAccessAdminEndpoints() throws Exception {
        registerUser("Standard User", "phase9-user@example.com", "SecretPass123!");
        String token = loginAndExtractToken("phase9-user@example.com", "SecretPass123!");

        mockMvc.perform(get("/api/admin/tickets")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("Access is denied."));
    }

    @Test
    void adminCanAccessAdminEndpoints() throws Exception {
        String token = loginAndExtractToken(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(get("/api/admin/tickets")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    private void registerUser(String name, String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "%s",
                      "email": "%s",
                      "password": "%s"
                    }
                    """.formatted(name, email, password)))
            .andExpect(status().isCreated());
    }

    private String loginAndExtractToken(String email, String password) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "%s",
                      "password": "%s"
                    }
                    """.formatted(email, password)))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        return jsonNode.get("token").asText();
    }
}
