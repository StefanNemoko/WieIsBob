package com.example.wieisbob.bobassignment;

import com.example.wieisbob.bobassignment.dto.BobAssignmentCount;
import com.example.wieisbob.group.Group;
import com.example.wieisbob.user.User;
import com.example.wieisbob.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class BobAssignmentService {

    private final BobAssignmentRepository bobAssignmentRepository;
    private final UserService userService;

    public BobAssignment assignBob(Group group, LocalDateTime assignedAt) {
        User nominatedUser = getNominatedUser(group);

        BobAssignment bobAssignment = new BobAssignment();
        bobAssignment.setUser(nominatedUser);
        bobAssignment.setGroup(group);
        bobAssignment.setAssignedAt(assignedAt);

        return bobAssignmentRepository.save(bobAssignment);
    }

    public BobAssignment getOneById(Long bobAssignmentId) {
        return bobAssignmentRepository.findById(bobAssignmentId)
                .orElseThrow(() -> new IllegalArgumentException("BobAssignment not found"));
    }

    public void deleteById(Long bobAssignmentId) {
        bobAssignmentRepository.deleteById(bobAssignmentId);
    }

    private User getNominatedUser(Group group) {
        List<BobAssignmentCount> counts =
                bobAssignmentRepository.countBobAssignmentsPerUserByGroup(group.getId());

        long lowestCount = counts.getFirst().getCount();

        List<BobAssignmentCount> eligible = counts.stream()
                .filter(b -> b.getCount() == lowestCount)
                .toList();

        BobAssignmentCount nominated = eligible.get(new Random().nextInt(eligible.size()));

        return userService.getOneById(nominated.getUserId());
    }
}
