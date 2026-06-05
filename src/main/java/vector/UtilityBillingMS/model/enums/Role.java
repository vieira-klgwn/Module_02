package vector.UtilityBillingMS.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@RequiredArgsConstructor
@Getter
public enum Role {
    ADMIN,
    OPERATOR,
    FINANCE,
    CUSTOMER;

    public List<SimpleGrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + name()));
    }
}
