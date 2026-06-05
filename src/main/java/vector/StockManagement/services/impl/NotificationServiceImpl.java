package vector.StockManagement.services.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vector.StockManagement.model.Notification;
import vector.StockManagement.model.enums.NotificationChannel;
import vector.StockManagement.model.enums.NotificationStatus;
import vector.StockManagement.model.enums.NotificationType;

import vector.StockManagement.repositories.NotificationRepository;
import vector.StockManagement.repositories.UserRepository;
import vector.StockManagement.services.NotificationSerivice;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NotificationServiceImpl implements NotificationSerivice {

    private final NotificationRepository notificationRepository;

    private final UserRepository userRepository;

    @Override
    public List<Notification> findAll(Long userId) {
        return userRepository.findById(userId).get().getNotifications();
    }

    @Override
    public Notification findById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }

    @Override
    public Notification save(Notification notification) {
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
        Notification saved = notificationRepository.save(notification);
        // Send notification asynchronously
        try {
            sendNotification(saved);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
        return saved;
    }

    @Override
    public void sendNotification(Notification notification) {
        try {
            if (notification.getChannel() == NotificationChannel.EMAIL) {
                sendEmailNotification(notification);
            } else if (notification.getChannel() == NotificationChannel.SMS) {
                sendSMSNotification(notification);
            }
            
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
            log.error("Failed to send notification {}: {}", notification.getId(), e.getMessage());
        }
    }

    private void sendEmailNotification(Notification notification) {
        // Placeholder for actual email sending logic
        log.info("Sending EMAIL notification: {} - {}", notification.getTitle(), notification.getMessage());
        // In a real implementation, you would integrate with email service like AWS SES, SendGrid, etc.
    }

    private void sendSMSNotification(Notification notification) {
        // Placeholder for actual SMS sending logic
        log.info("Sending SMS notification: {} - {}", notification.getTitle(), notification.getMessage());
        // In a real implementation, you would integrate with SMS service like Twilio, AWS SNS, etc.
    }
//
//    @Override
//    public void checkAndNotifyLowStock() {
//        List<Inventory> lowStockItems = inventoryRepository.findAll().stream()
//                .filter(Inventory::isLowStock)
//                .toList();
//
//        for (Inventory inventory : lowStockItems) {
//            Notification notification = new Notification();
//            notification.setType(NotificationType.LOW_STOCK_ALERT);
//            notification.setChannel(NotificationChannel.EMAIL);
//            notification.setTitle("Low Stock Alert");
//            notification.setSubject("Low Stock: " + inventory.getProduct().getName());
//            notification.setMessage(String.format(
//                    "Product %s (%s) is running low on stock. Current quantity: %d, Reorder level: %d",
//                    inventory.getProduct().getName(),
//                    inventory.getProduct().getSku(),
//                    inventory.getQtyOnHand(),
//                    inventory.getReorderLevel()
//            ));
//            notification.setTenant(inventory.getTenant());
//            notification.setReferenceType("INVENTORY");
//            notification.setReferenceId(inventory.getId());
//
//            save(notification);
//        }
//    }

    @Override
    public void delete(Long id) {
        notificationRepository.deleteById(id);
    }
}
