package vector.UtilityBillingMS.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vector.UtilityBillingMS.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByNationalId(String nationalId);

    default boolean existsByNationalIdAndIdNot(String nationalId, Long excludeId) {
        if (nationalId == null || nationalId.isBlank()) {
            return false;
        }
        if (excludeId == null) {
            return existsByNationalId(nationalId);
        }
        return existsByNationalId(nationalId) && findByNationalId(nationalId)
                .map(u -> !u.getId().equals(excludeId))
                .orElse(false);
    }

    Optional<User> findByNationalId(String nationalId);
}
