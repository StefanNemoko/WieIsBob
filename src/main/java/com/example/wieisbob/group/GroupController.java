package com.example.wieisbob.group;

import com.example.wieisbob.auth.SecurityUtils;
import com.example.wieisbob.exception.ForbiddenException;
import com.example.wieisbob.group.dto.AddGroupMemberRequest;
import com.example.wieisbob.group.dto.CreateGroupRequest;
import com.example.wieisbob.group.dto.GroupResponse;
import com.example.wieisbob.user.User;
import com.example.wieisbob.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;
    private final GroupMembershipRepository groupMembershipRepository;

    @PostMapping
    public GroupResponse create(@RequestBody @Valid CreateGroupRequest request) {
        User user = SecurityUtils.getAuthenticatedUser();

        Group group = groupService.Create(request);
        groupService.AddUserToGroup(user, group, GroupRole.OWNER);

        return new GroupResponse(group.getId(), group.getName(), group.getCreatedAt());
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<Void> addMember(
            @PathVariable Long groupId,
            @RequestBody @Valid AddGroupMemberRequest request) {

        User authenticatedUser = SecurityUtils.getAuthenticatedUser();
        Group group = groupService.getOneById(groupId);

        authorizeOwner(group, authenticatedUser);

        User userToAdd = userService.getOneById(request.userId());
        groupService.AddUserToGroup(userToAdd, group, GroupRole.PARTICIPANT);

        return ResponseEntity.ok().build();
    }

    /**
     * Only the group OWNER may add new members.
     */
    private void authorizeOwner(Group group, User user) {
        if (!groupMembershipRepository.existsByGroupIdAndUserIdAndRole(group.getId(), user.getId(), GroupRole.OWNER)) {
            throw new ForbiddenException("Only the group owner can add members to this group.");
        }
    }
}
