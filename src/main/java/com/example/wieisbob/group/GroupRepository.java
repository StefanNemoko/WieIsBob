package com.example.wieisbob.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group,Long> {
    List<Group> findByMembersId(Long membersId);
    boolean existsByIdAndMembersId(Long groupId, Long userId);
}
