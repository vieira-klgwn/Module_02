package vector.StockManagement.model;

public interface TenantScoped {
    Tenant getTenant();
    void setTenant(Tenant tenant);
}
