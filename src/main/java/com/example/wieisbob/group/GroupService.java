package com.example.wieisbob.group;

import com.example.wieisbob.group.dto.CreateGroupRequest;
import com.example.wieisbob.group.dto.GroupResponse;
import com.example.wieisbob.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public List<GroupResponse> GetAllByUserId(Long userId) {
        return groupRepository.findByMembershipsUserId(userId)
                .stream()
                .map(group -> new GroupResponse(
                        group.getId(),
                        group.getName(),
                        group.getCreatedAt()
                ))
                .toList();
    }

    public Group getOneById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }

    public Group Create(CreateGroupRequest request) {
        Group group = new Group();
        group.setName(request.name());
        group.setMemberships(new ArrayList<>());

        return groupRepository.save(group);
    }

    public void AddUserToGroup(User user, Group group, GroupRole role) {
        GroupMembership membership = new GroupMembership();
        membership.setGroup(group);
        membership.setUser(user);
        membership.setRole(role);

        groupMembershipRepository.save(membership);
    }
}
