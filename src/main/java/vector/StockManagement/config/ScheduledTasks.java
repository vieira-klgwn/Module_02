package vector.StockManagement.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vector.StockManagement.services.NotificationSerivice;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final NotificationSerivice notificationService;

    // Run every hour to check for low stock items
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void checkLowStockLevels() {
        log.info("Running scheduled low stock check...");
        try {
//            notificationService.checkAndNotifyLowStock();
            log.info("Low stock check completed successfully");
        } catch (Exception e) {
            log.error("Error during low stock check: {}", e.getMessage(), e);
        }
    }
}
