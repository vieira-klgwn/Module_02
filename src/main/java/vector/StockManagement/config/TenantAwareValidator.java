package vector.StockManagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import vector.StockManagement.model.TenantScoped;
import vector.StockManagement.model.User;

@Component
@RequiredArgsConstructor
public class TenantAwareValidator {

    public boolean validateTenantAccess(TenantScoped entity) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User currentUser)) {
            return false;
        }

        if (currentUser.getTenant() == null || entity.getTenant() == null) {
            return false;
        }

        return currentUser.getTenant().getId().equals(entity.getTenant().getId());
    }

    public boolean validateTenantAccess(Long tenantId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User currentUser)) {
            return false;
        }

        if (currentUser.getTenant() == null || tenantId == null) {
            return false;
        }

        return currentUser.getTenant().getId().equals(tenantId);
    }

    public Long getCurrentTenantId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof User currentUser && currentUser.getTenant() != null) {
            return currentUser.getTenant().getId();
        }

        return null;
    }
}
