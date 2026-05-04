package com.example.wieisbob.auth;

import com.example.wieisbob.BaseTest;
import com.example.wieisbob.auth.dto.LoginRequest;
import com.example.wieisbob.auth.dto.RegisterRequest;
import com.example.wieisbob.user.User;
import com.example.wieisbob.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@Transactional
@Sql(
        statements = {
                "ALTER TABLE users ALTER COLUMN id RESTART WITH 1",
                "ALTER TABLE bob_groups ALTER COLUMN id RESTART WITH 1",
                "ALTER TABLE bob_assignments ALTER COLUMN id RESTART WITH 1",
                "ALTER TABLE tokens ALTER COLUMN id RESTART WITH 1",
                "ALTER TABLE group_memberships ALTER COLUMN id RESTART WITH 1"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
class AuthControllerTest extends BaseTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Create a pre-existing user so the login tests have something to authenticate against.
        User user = new User();
        user.setEmail("existing@example.com");
        user.setName("Existing User");
        user.setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()));
        userRepository.save(user);
    }

    @Test
    void register_ReturnsAuthResponse_WhenValidRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("newuser@example.com", "New User", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearer").isNotEmpty())
                .andExpect(jsonPath("$.expiresAt").isNotEmpty());
    }

    @Test
    void login_ReturnsAuthResponse_WhenValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("existing@example.com", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearer").isNotEmpty())
                .andExpect(jsonPath("$.expiresAt").isNotEmpty());
    }
}
