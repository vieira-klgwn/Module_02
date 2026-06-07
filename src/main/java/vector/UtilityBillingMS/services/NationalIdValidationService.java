package vector.UtilityBillingMS.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vector.UtilityBillingMS.exceptions.BusinessException;
import vector.UtilityBillingMS.repositories.CustomerRepository;
import vector.UtilityBillingMS.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class NationalIdValidationService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    public void ensureUnique(String nationalId) {
        ensureUnique(nationalId, null, null);
    }

    public void ensureUnique(String nationalId, Long excludeUserId, Long excludeCustomerId) {
        if (nationalId == null || nationalId.isBlank()) {
            return;
        }
        if (userRepository.existsByNationalIdAndIdNot(nationalId, excludeUserId)) {
            throw new BusinessException("National ID already exists: " + nationalId);
        }
        if (customerRepository.existsByNationalIdAndIdNot(nationalId, excludeCustomerId)) {
            throw new BusinessException("National ID already exists: " + nationalId);
        }
    }
}
