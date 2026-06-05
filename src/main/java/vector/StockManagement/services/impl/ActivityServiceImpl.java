package vector.StockManagement.services.impl;

import jdk.jfr.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vector.StockManagement.model.Activity;
import vector.StockManagement.model.User;
import vector.StockManagement.model.enums.ActivityCategory;
import vector.StockManagement.repositories.ActivityRepository;
import vector.StockManagement.services.ActivityService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor//you can add in @Transacional to make sure you handle things actions that shouls occur simultaneously
public class ActivityServiceImpl implements ActivityService {
    private final ActivityRepository activityRepository;

    @Override
    public Activity createActivity(User user, String activityName, ActivityCategory category, String activityDescription) {
        Activity activity = new Activity();
        activity.setUser(user);
        activity.setActivityName(activityName);
        activity.setDescription(activityName);
        activity.setDateTime(LocalDateTime.now());
        activity.setCategory(category);
        activity.setDescription(activityDescription);
        return activityRepository.save(activity);
    }

    @Override
    public List<Activity> getActivitiesByUser(User user) {
        return activityRepository.findByUser(user);
    }

    @Override
    public Activity updateActivity(Long id, Activity activity) {
        if (activityRepository.findById(id).isPresent()) {
            Activity activity1 = activityRepository.findById(id).get();
            activity1.setActivityName(activity.getActivityName());
            activity1.setDescription(activity.getActivityName());
            activity1.setDateTime(activity.getDateTime());
            activity1.setCategory(activity.getCategory());
            return activityRepository.save(activity1);
        } else {
            return null;
        }
    }

    @Override
    public void deleteActivity(Long id) {

        if (activityRepository.findById(id).isPresent()) {
            activityRepository.deleteById(id);
        }
    }

    @Override
    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }


}
