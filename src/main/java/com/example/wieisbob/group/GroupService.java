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

    public List<GroupResponse> GetAllByUserId(Long userId) {
        return groupRepository.findByMembersId(userId)
                .stream()
                .map(group -> new GroupResponse(
                        group.getId(),
                        group.getName(),
                        group.getCreatedAt()
                ))
                .toList();
    }

    public Group getOneById(Long groupId) {
        return groupRepository.findById(groupId).orElseThrow(()->new IllegalArgumentException("Group not found"));
    }

    public Group Create(CreateGroupRequest request) {
        Group group = new Group();
        group.setName(request.name());
        group.setMembers(new ArrayList<>());

        return groupRepository.save(group);
    }

    public void AddUserToGroup(User user, Group group) {
        group.getMembers().add(user);
        groupRepository.save(group);
    }
}

