package vector.UtilityBillingMS.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import vector.UtilityBillingMS.exceptions.BusinessException;
import vector.UtilityBillingMS.model.Bill;
import vector.UtilityBillingMS.model.User;
import vector.UtilityBillingMS.model.enums.Role;
import vector.UtilityBillingMS.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomerAccessService {

    private final UserRepository userRepository;

    public Long resolveCustomerId(User user) {
        User loaded = userRepository.findByIdWithCustomer(user.getId())
                .orElseThrow(() -> new BusinessException("User not found"));
        if (loaded.getCustomer() == null) {
            throw new BusinessException("No customer profile linked to this account");
        }
        return loaded.getCustomer().getId();
    }

    public void ensureOwnCustomer(User user, Long customerId) {
        if (user.getRole() == Role.CUSTOMER) {
            Long ownCustomerId = resolveCustomerId(user);
            if (!ownCustomerId.equals(customerId)) {
                throw new AccessDeniedException("Access denied: you can only access your own customer data");
            }
        }
    }

    public void ensureOwnBill(User user, Bill bill) {
        if (user.getRole() == Role.CUSTOMER) {
            Long ownCustomerId = resolveCustomerId(user);
            if (!bill.getCustomer().getId().equals(ownCustomerId)) {
                throw new AccessDeniedException("Access denied: you can only access your own bills");
            }
        }
    }

    public boolean isCustomer(User user) {
        return user.getRole() == Role.CUSTOMER;
    }
}
