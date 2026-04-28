package com.example.wieisbob.bobassignment;

import com.example.wieisbob.bobassignment.dto.BobAssignmentCount;
import com.example.wieisbob.group.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BobAssignmentRepository extends JpaRepository<BobAssignment, Long> {

    @Query(value = "SELECT gu.user_id AS userId, COUNT(ba.user_id) AS count " +
        "FROM bob_assignments ba " +
        "RIGHT JOIN group_users gu ON ba.user_id = gu.user_id " +
        "GROUP BY gu.user_id", nativeQuery = true)
    List<BobAssignmentCount> countBobAssignmentsPerUserByGroup(@Param("group") Group group);
}
