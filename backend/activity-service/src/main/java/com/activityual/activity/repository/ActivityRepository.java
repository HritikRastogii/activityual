package com.activityual.activity.repository;

import com.activityual.activity.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {
    List<Activity> findAllByUserEmailOrderByCreatedAtDesc(String userEmail);
    Optional<Activity> findByIdAndUserEmail(UUID id, String userEmail);
}
