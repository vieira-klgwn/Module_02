package vector.UtilityBillingMS.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vector.UtilityBillingMS.model.Bill;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByBillReference(String billReference);
    List<Bill> findByCustomerId(Long customerId);
    boolean existsByMeterReadingId(Long meterReadingId);
}
