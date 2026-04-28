package com.example.wieisbob.user;

import com.example.wieisbob.auth.SecurityUtils;
import com.example.wieisbob.exception.ForbiddenException;
import com.example.wieisbob.group.GroupService;
import com.example.wieisbob.group.dto.GroupResponse;
import com.example.wieisbob.user.dto.UpdateUserRequest;
import com.example.wieisbob.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final GroupService groupService;

    @PatchMapping("/{userId}")
    public ResponseEntity<Void> update(@PathVariable Long userId, @RequestBody @Valid UpdateUserRequest request) {
        authorize(userId);

        userService.update(userId, request);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    public UserResponse get(@PathVariable Long userId) {
        authorize(userId);

        User user = userService.getOneById(userId);
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
    }

    @GetMapping("/{userId}/groups")
    public List<GroupResponse> getGroups(@PathVariable Long userId) {
        authorize(userId);

        return groupService.GetAllByUserId(userId);
    }


    private void authorize(Long userId) {
        User authenticatedUser = SecurityUtils.getAuthenticatedUser();
        if (!authenticatedUser.getId().equals(userId)) {
            throw new ForbiddenException("You do not have access to this resource.");
        }
    }
}
