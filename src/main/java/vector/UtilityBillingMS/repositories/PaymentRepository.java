package vector.UtilityBillingMS.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vector.UtilityBillingMS.model.Payment;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBillReference(String billReference);

    List<Payment> findByBillId(Long billId);

    @Query("SELECT p FROM Payment p JOIN p.bill b WHERE b.customer.id = :customerId ORDER BY p.createdAt DESC")
    List<Payment> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT p FROM Payment p JOIN FETCH p.bill b WHERE p.id = :id")
    Optional<Payment> findByIdWithBill(@Param("id") Long id);
}
