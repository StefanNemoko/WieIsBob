package com.example.wieisbob.bobassignment;

import com.example.wieisbob.auth.SecurityUtils;
import com.example.wieisbob.bobassignment.dto.BobAssignmentRequest;
import com.example.wieisbob.bobassignment.dto.BobAssignmentResponse;
import com.example.wieisbob.exception.UnauthorizedException;
import com.example.wieisbob.group.Group;
import com.example.wieisbob.group.GroupRepository;
import com.example.wieisbob.group.GroupService;
import com.example.wieisbob.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bobassignment")
@RequiredArgsConstructor
public class BobAssignmentController {

    private final BobAssignmentService bobAssignmentService;
    private final GroupService groupService;
    private final GroupRepository groupRepository;

    @PostMapping("/{groupId}/bob")
    public BobAssignmentResponse assignBob(@PathVariable Long groupId, @RequestBody @Valid BobAssignmentRequest request) {
        Group group = groupService.getOneById(groupId);

        authorize(group);

        BobAssignment assignedBob = bobAssignmentService.assignBob(group, request.assignedAt());

        return new BobAssignmentResponse(assignedBob.getId(), assignedBob.getUser().getId(), assignedBob.getGroup().getId(), assignedBob.getAssignedAt());
    }

    @DeleteMapping("{bobAssignmentId}")
    public ResponseEntity<?> deleteBobAssignment(@PathVariable Long bobAssignmentId) {
        BobAssignment bobAssignment = bobAssignmentService.getOneById(bobAssignmentId);

        Group group = bobAssignment.getGroup();

        authorize(group);

        bobAssignmentService.deleteById(bobAssignmentId);

        return ResponseEntity.ok().build();
    }

    /**
     * Not as aspect, because the way you can retrieve a group can be using multiple ways.
     * @param group
     */
    private void authorize(Group group) {
        User authenticatedUser = SecurityUtils.getAuthenticatedUser();

        if (!groupRepository.existsByIdAndMembersId(group.getId(), authenticatedUser.getId())) {
            throw new UnauthorizedException("No permission to assign a bob to this group.");
        }

        //TODO:: assign roles to users and allow only specific users to delete / assign a bob.
    }

}
