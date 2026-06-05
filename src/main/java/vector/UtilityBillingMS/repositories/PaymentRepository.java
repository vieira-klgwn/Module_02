package vector.UtilityBillingMS.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vector.UtilityBillingMS.model.Payment;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBillReference(String billReference);
    List<Payment> findByBillId(Long billId);
}
