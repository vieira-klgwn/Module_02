package vector.UtilityBillingMS.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vector.UtilityBillingMS.model.Customer;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByNationalId(String nationalId);
    Optional<Customer> findByNationalId(String nationalId);

    default boolean existsByNationalIdAndIdNot(String nationalId, Long excludeId) {
        if (nationalId == null || nationalId.isBlank()) {
            return false;
        }
        if (excludeId == null) {
            return existsByNationalId(nationalId);
        }
        return existsByNationalId(nationalId) && findByNationalId(nationalId)
                .map(c -> !c.getId().equals(excludeId))
                .orElse(false);
    }
}
