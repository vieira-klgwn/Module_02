package vector.StockManagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vector.StockManagement.model.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
}
