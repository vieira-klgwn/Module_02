package vector.UtilityBillingMS.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vector.UtilityBillingMS.model.Tariff;
import vector.UtilityBillingMS.model.enums.MeterType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {
    @Query("SELECT t FROM Tariff t WHERE t.utilityType = :type AND t.effectiveFrom <= :date AND (t.effectiveTo IS NULL OR t.effectiveTo >= :date) ORDER BY t.version DESC")
    List<Tariff> findApplicableTariffs(@Param("type") MeterType type, @Param("date") LocalDate date);

    default Optional<Tariff> findApplicableTariff(MeterType type, LocalDate date) {
        return findApplicableTariffs(type, date).stream().findFirst();
    }

    Optional<Tariff> findTopByUtilityTypeOrderByVersionDesc(MeterType utilityType);
}
