package vector.UtilityBillingMS.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vector.UtilityBillingMS.model.Notification;
import vector.UtilityBillingMS.model.User;
import vector.UtilityBillingMS.services.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<List<Notification>> findAll() {
        return ResponseEntity.ok(notificationService.findAll());
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    public ResponseEntity<List<Notification>> findByCustomer(
            @PathVariable Long customerId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificationService.findByCustomerIdForUser(customerId, user));
    }
}
