package com.example.wieisbob.bobassignment;

import com.example.wieisbob.bobassignment.dto.BobAssignmentCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BobAssignmentRepository extends JpaRepository<BobAssignment, Long> {

    /**
     * Returns each group member with a count of how many times they have been assigned
     * as bob within that group, ordered ascending so the least-frequent bob comes first.
     */
    @Query(value =
            "SELECT gm.user_id AS userId, COUNT(ba.id) AS count " +
            "FROM group_memberships gm " +
            "LEFT JOIN bob_assignments ba ON ba.user_id = gm.user_id AND ba.group_id = gm.group_id " +
            "WHERE gm.group_id = :groupId " +
            "GROUP BY gm.user_id " +
            "ORDER BY count ASC",
            nativeQuery = true)
    List<BobAssignmentCount> countBobAssignmentsPerUserByGroup(@Param("groupId") Long groupId);
}
