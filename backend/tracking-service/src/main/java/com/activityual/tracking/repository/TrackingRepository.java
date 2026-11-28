package com.activityual.tracking.repository;

import com.activityual.tracking.entity.TrackingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrackingRepository extends JpaRepository<TrackingRecord, UUID> {
    List<TrackingRecord> findAllByUserEmailOrderByTrackingDateDesc(String email);
    List<TrackingRecord> findAllByUserEmailAndTrackingDateOrderByCreatedAtDesc(
            String email, LocalDate date);
    List<TrackingRecord> findAllByUserEmailAndActivityIdOrderByTrackingDateDesc(
            String email, UUID activityId);
    Optional<TrackingRecord> findByIdAndUserEmail(UUID id, String email);
    boolean existsByUserEmailAndActivityIdAndTrackingDate(
            String email, UUID activityId, LocalDate date);
    Optional<TrackingRecord> findByUserEmailAndActivityIdAndTrackingDate(
            String email, UUID activityId, LocalDate date);
}
