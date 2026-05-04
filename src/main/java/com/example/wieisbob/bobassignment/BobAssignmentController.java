package com.example.wieisbob.bobassignment;

import com.example.wieisbob.auth.SecurityUtils;
import com.example.wieisbob.bobassignment.dto.BobAssignmentRequest;
import com.example.wieisbob.bobassignment.dto.BobAssignmentResponse;
import com.example.wieisbob.exception.UnauthorizedException;
import com.example.wieisbob.group.Group;
import com.example.wieisbob.group.GroupMembershipRepository;
import com.example.wieisbob.group.GroupService;
import com.example.wieisbob.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bobassignment")
@RequiredArgsConstructor
public class BobAssignmentController {

    private final BobAssignmentService bobAssignmentService;
    private final GroupService groupService;
    private final GroupMembershipRepository groupMembershipRepository;

    @PostMapping("/{groupId}/bob")
    public BobAssignmentResponse assignBob(
            @PathVariable Long groupId,
            @RequestBody @Valid BobAssignmentRequest request) {

        Group group = groupService.getOneById(groupId);
        authorize(group);

        BobAssignment assignedBob = bobAssignmentService.assignBob(group, request.assignedAt());

        return new BobAssignmentResponse(
                assignedBob.getId(),
                assignedBob.getUser().getId(),
                assignedBob.getGroup().getId(),
                assignedBob.getAssignedAt()
        );
    }

    @DeleteMapping("/{bobAssignmentId}")
    public ResponseEntity<Void> deleteBobAssignment(@PathVariable Long bobAssignmentId) {
        BobAssignment bobAssignment = bobAssignmentService.getOneById(bobAssignmentId);
        authorize(bobAssignment.getGroup());

        bobAssignmentService.deleteById(bobAssignmentId);

        return ResponseEntity.ok().build();
    }

    /**
     * Any group member (regardless of role) may assign or delete a bob.
     */
    private void authorize(Group group) {
        User authenticatedUser = SecurityUtils.getAuthenticatedUser();

        if (!groupMembershipRepository.existsByGroupIdAndUserId(group.getId(), authenticatedUser.getId())) {
            throw new UnauthorizedException("No permission to manage bob assignments for this group.");
        }
    }
}
