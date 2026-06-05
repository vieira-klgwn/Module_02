package vector.StockManagement.services;

import vector.StockManagement.model.Activity;
import vector.StockManagement.model.User;
import vector.StockManagement.model.enums.ActivityCategory;


import java.time.LocalDateTime;
import java.util.List;

public interface ActivityService {
    Activity createActivity(User user, String activityName, ActivityCategory category, String description);
    List<Activity> getActivitiesByUser(User user);
    Activity updateActivity(Long id, Activity activity);
    void deleteActivity(Long id);
    List<Activity> getAllActivities();

}
