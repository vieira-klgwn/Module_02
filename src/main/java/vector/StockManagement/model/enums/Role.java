package vector.StockManagement.model.enums;



import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


import java.util.*;
import java.util.stream.Collectors;


import static vector.StockManagement.model.enums.Permission.*;


@RequiredArgsConstructor
public enum Role {
    USER(Collections.emptySet()),
    SUPER_ADMIN(Set.of(
            ADMIN_READ, ADMIN_CREATE, ADMIN_UPDATE, ADMIN_DELETE,
            MANAGER_READ, MANAGER_DELETE, MANAGER_UPDATE, MANAGER_CREATE


    )),
    ADMIN(
            Set.of(ADMIN_READ, ADMIN_CREATE, ADMIN_UPDATE, ADMIN_DELETE,
                    MANAGER_READ, MANAGER_DELETE, MANAGER_UPDATE, MANAGER_CREATE

            )
    ),
    MANAGER(
            Set.of(MANAGER_READ, MANAGER_CREATE, MANAGER_UPDATE

            )
    ),
    MANAGING_DIRECTOR(Set.of(ADMIN_READ, ADMIN_CREATE, ADMIN_UPDATE, ADMIN_DELETE)),
    DISTRIBUTOR(Set.of(MANAGER_READ, MANAGER_CREATE, MANAGER_UPDATE)),
    ACCOUNTANT(Set.of(MANAGER_READ)),
    ACCOUNTANT_AT_STORE(Set.of(MANAGER_READ)),
    SALES_MANAGER(Set.of(MANAGER_READ, MANAGER_CREATE, MANAGER_UPDATE)),
    STORE_MANAGER(Set.of(MANAGER_READ, MANAGER_CREATE, MANAGER_UPDATE)),
    WAREHOUSE_MANAGER(Set.of(MANAGER_READ, MANAGER_CREATE, MANAGER_UPDATE)),
    RETAILER(Set.of(MANAGER_READ)),
    WHOLE_SALER(Set.of(MANAGER_READ));



    @Getter
    private final Set<Permission> permissions;



    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + name()));
        return authorities;
    }


}
