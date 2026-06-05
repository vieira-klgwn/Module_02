package vector.UtilityBillingMS.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vector.UtilityBillingMS.model.MeterReading;

import java.util.Optional;

@Repository
public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {
    boolean existsByMeterIdAndBillingMonthAndBillingYear(Long meterId, int month, int year);
    Optional<MeterReading> findTopByMeterIdOrderByReadingDateDesc(Long meterId);
}
