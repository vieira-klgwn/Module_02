package vector.UtilityBillingMS.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vector.UtilityBillingMS.model.Meter;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeterRepository extends JpaRepository<Meter, Long> {
    boolean existsByMeterNumber(String meterNumber);
    Optional<Meter> findByMeterNumber(String meterNumber);
    List<Meter> findByCustomerId(Long customerId);
}
