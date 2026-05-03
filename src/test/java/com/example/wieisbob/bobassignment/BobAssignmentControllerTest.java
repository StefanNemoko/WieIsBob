package com.example.wieisbob.bobassignment;

import com.example.wieisbob.BaseTest;
import com.example.wieisbob.auth.AuthService;
import com.example.wieisbob.auth.Token;
import com.example.wieisbob.auth.TokenRepository;
import com.example.wieisbob.bobassignment.dto.BobAssignmentRequest;
import com.example.wieisbob.group.Group;
import com.example.wieisbob.group.GroupRepository;
import com.example.wieisbob.group.GroupService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class BobAssignmentControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BobAssignmentRepository bobAssignmentRepository;

    @Autowired
    private GroupRepository groupRepository;



    @MockitoBean
    private BobAssignmentService bobAssignmentService;

    @MockitoBean
    private GroupService groupService;

    private String bearerToken;
    private User authenticatedUser;
    private Group group;



    @BeforeEach
    void setUp() {
        authenticatedUser = new User();
        authenticatedUser.setName("Test user");
        authenticatedUser.setEmail("test@example.com");
        authenticatedUser.setPassword("hash_password");
        authenticatedUser = userRepository.save(authenticatedUser);

        group = new Group();
        group.setName("Test group");
        group = groupRepository.save(group);

        // Save the token in the DB so JwtFilter can find it when calling the endpoints.
        Token jwt = authService.createToken(authenticatedUser);
        bearerToken = jwt.getToken();
    }

    @Test
    void assignBob_OkResponse_WhenUserIsMemberOfGroup() throws Exception {
        BobAssignmentRequest request = new BobAssignmentRequest(LocalDateTime.now());

        // Add authenticated user as a member so authorize() passes
        group.setMembers(new ArrayList<>(List.of(authenticatedUser)));
        group = groupRepository.save(group);

        // Create a bob assignment record
        BobAssignment bobAssignment = new BobAssignment();
        bobAssignment.setUser(authenticatedUser);
        bobAssignment.setGroup(group);
        bobAssignment.setAssignedAt(request.assignedAt());
        bobAssignmentRepository.save(bobAssignment);

        // when we call these functions with the given parameters, we should return the given data, otherwise it tries to call to a database with non existing values.
        when(groupService.getOneById(group.getId())).thenReturn(group);
        when(bobAssignmentService.assignBob(group, request.assignedAt())).thenReturn(bobAssignment);

        mockMvc.perform(post("/bobassignment/1/bob")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.groupId").value(1L))
                .andExpect(jsonPath("$.userId").value(1L)); // Yes it is the same as authenticatedUser.getId() but, wtest should always validate A matches A and not have any logic in them.
    }

    @Test
    void assignBob_UnauthorizedResponse_WhenUserIsNotMemberOfGroup() throws Exception {
        BobAssignmentRequest request = new BobAssignmentRequest(LocalDateTime.now());

        when(groupService.getOneById(1L)).thenReturn(group);

        // because we said the function existsByIdAndMembersId should return false, we should not be authorized.
        mockMvc.perform(post("/bobassignment/1/bob")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void assignBob_BadRequestResponse_whenRequestBodyIsInvalid() throws Exception {
        mockMvc.perform(post("/bobassignment/1/bob")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteBobAssignment_ReturnsOk_WhenUserIsMemberOfGroup() throws Exception {
        // Add authenticated user as a member so authorize() passes
        group.setMembers(new ArrayList<>(List.of(authenticatedUser)));
        group = groupRepository.save(group);

        BobAssignment bobAssignment = new BobAssignment();
        bobAssignment.setUser(authenticatedUser);
        bobAssignment.setGroup(group);

        when(bobAssignmentService.getOneById(1L)).thenReturn(bobAssignment);

        mockMvc.perform(delete("/bobassignment/1")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk());
    }

    @Test
    void deleteBobAssignment_ReturnsUnauthorized_WhenUserIsNotMemberOfGroup() throws Exception {
        // group has no members, so existsByIdAndMembersId returns false
        BobAssignment bobAssignment = new BobAssignment();
        bobAssignment.setUser(authenticatedUser);
        bobAssignment.setGroup(group);

        when(bobAssignmentService.getOneById(1L)).thenReturn(bobAssignment);

        mockMvc.perform(delete("/bobassignment/1")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteBobAssignment_ReturnsUnauthorized_WhenNoToken() throws Exception {
        mockMvc.perform(delete("/bobassignment/1"))
                .andExpect(status().isUnauthorized());
    }
}