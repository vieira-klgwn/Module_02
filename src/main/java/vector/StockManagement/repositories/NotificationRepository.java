package vector.StockManagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vector.StockManagement.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
