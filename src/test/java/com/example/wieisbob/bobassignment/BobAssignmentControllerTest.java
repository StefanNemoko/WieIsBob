package com.example.wieisbob.bobassignment;

import com.example.wieisbob.BaseTest;
import com.example.wieisbob.auth.AuthService;
import com.example.wieisbob.auth.SecurityUtils;
import com.example.wieisbob.auth.Token;
import com.example.wieisbob.bobassignment.dto.BobAssignmentRequest;
import com.example.wieisbob.group.Group;
import com.example.wieisbob.group.GroupRepository;
import com.example.wieisbob.group.GroupService;
import com.example.wieisbob.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
class BobAssignmentControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @MockitoBean
    private BobAssignmentService bobAssignmentService;

    @MockitoBean
    private GroupService groupService;

    @MockitoBean
    private GroupRepository groupRepository;

    private String bearerToken;
    private User authenticatedUser;
    private Group group;


    @BeforeEach
    void setUp() {
        authenticatedUser = new User();
        authenticatedUser.setId(1L);
        authenticatedUser.setName("Test user");
        authenticatedUser.setEmail("test@example.com");

        group = new Group();
        group.setId(1L);
        group.setName("Test group");

        Token jwt = authService.createToken(authenticatedUser);
        bearerToken = jwt.getToken();

        // create a new authentication object to mock.
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(authenticatedUser, null, List.of());
        // Mock the authenticated user in our security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void assignBob_OkResponse_WhenUserIsMemberOfGroup() throws Exception {
        BobAssignmentRequest request = new BobAssignmentRequest(LocalDateTime.now());

        // Create a bob assignment record
        BobAssignment bobAssignment = new BobAssignment();
        bobAssignment.setId(1L);
        bobAssignment.setUser(authenticatedUser);
        bobAssignment.setGroup(group);
        bobAssignment.setAssignedAt(request.assignedAt());

        // when we call these functions with the given parameters, we should return the given data, otherwise it tries to call to a database with non existing values.
        when(groupService.getOneById(1L)).thenReturn(group);
        when(groupRepository.existsByIdAndMembersId(1L, 1L)).thenReturn(true);
        when(bobAssignmentService.assignBob(group, request.assignedAt())).thenReturn(bobAssignment);

        mockMvc.perform(post("/bobassignment/1/bob")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.groupId").value(1L))
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    void assignBob_UnauthorizedResponse_WhenUserIsNotMemberOfGroup() throws Exception {
        BobAssignmentRequest request = new BobAssignmentRequest(LocalDateTime.now());

        when(groupService.getOneById(1L)).thenReturn(group);
        when(groupRepository.existsByIdAndMembersId(1L, 1L)).thenReturn(false);

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
}