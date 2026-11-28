package com.activityual.activity.service;

import com.activityual.activity.dto.ActivityRequest;
import com.activityual.activity.dto.ActivityResponse;
import com.activityual.activity.entity.Activity;
import com.activityual.activity.exception.ResourceNotFoundException;
import com.activityual.activity.repository.ActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ActivityService {

    private final ActivityRepository repository;

    public ActivityService(ActivityRepository repository) {
        this.repository = repository;
    }

    public ActivityResponse create(String email, ActivityRequest request) {
        Activity activity = new Activity();
        activity.setUserEmail(email);
        applyRequest(activity, request);
        return ActivityResponse.from(repository.save(activity));
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getAll(String email) {
        return repository.findAllByUserEmailOrderByCreatedAtDesc(email)
                .stream().map(ActivityResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ActivityResponse getById(String email, UUID id) {
        return ActivityResponse.from(findOwnedActivity(email, id));
    }

    public ActivityResponse update(String email, UUID id, ActivityRequest request) {
        Activity activity = findOwnedActivity(email, id);
        applyRequest(activity, request);
        return ActivityResponse.from(repository.save(activity));
    }

    public void delete(String email, UUID id) {
        Activity activity = findOwnedActivity(email, id);
        repository.delete(activity);
    }

    private Activity findOwnedActivity(String email, UUID id) {
        return repository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Activity not found: " + id));
    }

    private void applyRequest(Activity activity, ActivityRequest request) {
        activity.setName(request.name());
        activity.setDescription(request.description());
        activity.setFrequency(request.frequency());
        activity.setCategory(request.category());
        activity.setNotes(request.notes());

        if (request.active() != null) {
            activity.setActive(request.active());
        }
    }
}
