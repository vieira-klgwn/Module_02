package vector.StockManagement.services;



import vector.StockManagement.auth.AuthenticationResponse;
import vector.StockManagement.model.Tenant;
import vector.StockManagement.model.dto.TenantDTO;

import java.util.List;

public interface TenantService {
    List<Tenant> findAll();
    Tenant findById(Long id);
    AuthenticationResponse save(TenantDTO tenantDTO);
    void delete(Long id);
    Tenant update(Long id, Tenant tenant);
}