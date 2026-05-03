package com.example.wieisbob.user;

import com.example.wieisbob.BaseTest;
import com.example.wieisbob.auth.AuthService;
import com.example.wieisbob.auth.Token;
import com.example.wieisbob.group.GroupService;
import com.example.wieisbob.group.dto.GroupResponse;
import com.example.wieisbob.user.dto.UpdateUserRequest;
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
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class UserControllerTest extends BaseTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    // GroupService is mocked because it is only used for GET /user/{userId}/groups,
    // which lets us control what groups are returned without needing real group data.
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
    void update_ReturnsOk_WhenUpdatingOwnProfile() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("New Name", null);

        mockMvc.perform(patch("/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk());
    }

    @Test
    void update_ReturnsForbidden_WhenUpdatingOtherProfile() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("New Name", null);

        mockMvc.perform(patch("/user/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_ReturnsUnauthorized_WhenNoToken() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("New Name", null);

        mockMvc.perform(patch("/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void get_ReturnsUserResponse_WhenGettingOwnProfile() throws Exception {
        mockMvc.perform(get("/user/1")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test user"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void get_ReturnsForbidden_WhenGettingOtherProfile() throws Exception {
        mockMvc.perform(get("/user/2")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getGroups_ReturnsGroupList_WhenGettingOwnGroups() throws Exception {
        List<GroupResponse> groups = List.of(
                new GroupResponse(1L, "Test group", LocalDateTime.now())
        );
        when(groupService.GetAllByUserId(1L)).thenReturn(groups);

        mockMvc.perform(get("/user/1/groups")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test group"));
    }

    @Test
    void getGroups_ReturnsForbidden_WhenGettingOtherGroups() throws Exception {
        mockMvc.perform(get("/user/2/groups")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isForbidden());
    }
}
