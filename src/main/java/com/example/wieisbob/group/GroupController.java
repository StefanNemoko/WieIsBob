package com.example.wieisbob.group;

import com.example.wieisbob.auth.SecurityUtils;
import com.example.wieisbob.group.dto.CreateGroupRequest;
import com.example.wieisbob.group.dto.GroupResponse;
import com.example.wieisbob.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping()
    public GroupResponse create(@RequestBody @Valid CreateGroupRequest request) {
        User user = SecurityUtils.getAuthenticatedUser();

        Group group = groupService.Create(request);
        groupService.AddUserToGroup(user, group);

        return new GroupResponse(group.getId(), group.getName(), group.getCreatedAt());
    }
}
