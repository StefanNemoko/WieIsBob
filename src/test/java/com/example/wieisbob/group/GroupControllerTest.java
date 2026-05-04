package com.example.wieisbob.group;

import com.example.wieisbob.BaseTest;
import com.example.wieisbob.auth.AuthService;
import com.example.wieisbob.auth.Token;
import com.example.wieisbob.group.dto.AddGroupMemberRequest;
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
import java.util.ArrayList;

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
                "ALTER TABLE tokens ALTER COLUMN id RESTART WITH 1",
                "ALTER TABLE group_memberships ALTER COLUMN id RESTART WITH 1"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
class GroupControllerTest extends BaseTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMembershipRepository groupMembershipRepository;

    // GroupService is mocked so the create() logic is controlled; real repositories
    // handle the membership checks in addMember().
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

    // ── POST /group ─────────────────────────────────────────────────────────────

    @Test
    void create_ReturnsGroupResponse_WhenValidRequest() throws Exception {
        CreateGroupRequest request = new CreateGroupRequest("Test group");

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
        mockMvc.perform(post("/group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateGroupRequest("Test group"))))
                .andExpect(status().isUnauthorized());
    }

    // ── POST /group/{groupId}/members ────────────────────────────────────────────

    @Test
    void addMember_ReturnsOk_WhenOwnerAddsUser() throws Exception {
        // Persist a real group so the membership lookup has a valid group_id.
        Group group = new Group();
        group.setName("Test group");
        group.setMemberships(new ArrayList<>());
        group = groupRepository.save(group);

        // Make the authenticated user the OWNER.
        GroupMembership ownership = new GroupMembership();
        ownership.setGroup(group);
        ownership.setUser(authenticatedUser);
        ownership.setRole(GroupRole.OWNER);
        groupMembershipRepository.save(ownership);

        // A second user to add as participant.
        User newMember = new User();
        newMember.setName("New Member");
        newMember.setEmail("member@example.com");
        newMember.setPassword("password");
        userRepository.save(newMember);

        when(groupService.getOneById(group.getId())).thenReturn(group);

        mockMvc.perform(post("/group/" + group.getId() + "/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddGroupMemberRequest(newMember.getId())))
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk());
    }

    @Test
    void addMember_ReturnsForbidden_WhenNonOwnerTriesToAddUser() throws Exception {
        // Persist a group but give the authenticated user no membership (no OWNER role).
        Group group = new Group();
        group.setName("Test group");
        group.setMemberships(new ArrayList<>());
        group = groupRepository.save(group);

        when(groupService.getOneById(group.getId())).thenReturn(group);

        mockMvc.perform(post("/group/" + group.getId() + "/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddGroupMemberRequest(99L)))
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void addMember_ReturnsUnauthorized_WhenNoToken() throws Exception {
        mockMvc.perform(post("/group/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddGroupMemberRequest(2L))))
                .andExpect(status().isUnauthorized());
    }
}
