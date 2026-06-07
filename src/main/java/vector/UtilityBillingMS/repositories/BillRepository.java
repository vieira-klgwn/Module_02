package vector.UtilityBillingMS.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vector.UtilityBillingMS.model.Bill;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByBillReference(String billReference);

    @Query("SELECT b FROM Bill b JOIN FETCH b.customer WHERE b.billReference = :reference")
    Optional<Bill> findByBillReferenceWithCustomer(@Param("reference") String reference);

    @Query("SELECT b FROM Bill b JOIN FETCH b.customer WHERE b.id = :id")
    Optional<Bill> findByIdWithCustomer(@Param("id") Long id);

    List<Bill> findByCustomerId(Long customerId);
    boolean existsByMeterReadingId(Long meterReadingId);
}
