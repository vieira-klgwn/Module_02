package vector.StockManagement.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import vector.StockManagement.model.Activity;
import vector.StockManagement.services.ActivityService;
import vector.StockManagement.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;
    private final UserService userService;

//    @PostMapping
//    public ResponseEntity<Activity> createActivity(@RequestBody Activity activity) {
//        return ResponseEntity.ok(activityService.createActivity(activity.getUser(),activity.getActivityName(),activity.getCategory()));
//    }

    @GetMapping
    public ResponseEntity<List<Activity>> getActivities() {
        return ResponseEntity.ok(activityService.getAllActivities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<Activity>> getActivityByUSer(@PathVariable Long id) {
        if (userService.getUserById(id).isPresent()) {
            List <Activity> activities= activityService.getActivitiesByUser(userService.getUserById(id).get());
            return ResponseEntity.ok(activities);
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Activity> updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        return ResponseEntity.ok(activityService.updateActivity(id, activity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Activity> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return ResponseEntity.ok().build();
    }


}
