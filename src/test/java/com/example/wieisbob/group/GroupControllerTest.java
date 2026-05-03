package com.example.wieisbob.group;

import com.example.wieisbob.BaseTest;
import com.example.wieisbob.auth.AuthService;
import com.example.wieisbob.auth.Token;
import com.example.wieisbob.group.dto.CreateGroupRequest;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
                "ALTER TABLE tokens ALTER COLUMN id RESTART WITH 1"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
class GroupControllerTest extends BaseTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    // GroupService is mocked so we can control what Create() returns without
    // needing actual group data persisted in the database.
    @MockitoBean
    private GroupService groupService;

    private String bearerToken;
    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        authenticatedUser = new User();
        authenticatedUser.setName("Test user");
        authenticatedUser.setEmail("test@example.com");
        authenticatedUser.setPassword("hash_password");
        authenticatedUser = userRepository.save(authenticatedUser);

        // Save the token in the DB so JwtFilter can find it when calling the endpoints.
        Token jwt = authService.createToken(authenticatedUser);
        bearerToken = jwt.getToken();
    }

    @Test
    void create_ReturnsGroupResponse_WhenValidRequest() throws Exception {
        CreateGroupRequest request = new CreateGroupRequest("Test group");

        // Return a fully populated Group so the controller can build a GroupResponse.
        Group group = new Group();
        group.setId(1L);
        group.setName("Test group");
        group.setCreatedAt(LocalDateTime.now());
        when(groupService.Create(any(CreateGroupRequest.class))).thenReturn(group);

        mockMvc.perform(post("/group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test group"));
    }

    @Test
    void create_ReturnsUnauthorized_WhenNoToken() throws Exception {
        CreateGroupRequest request = new CreateGroupRequest("Test group");

        mockMvc.perform(post("/group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
