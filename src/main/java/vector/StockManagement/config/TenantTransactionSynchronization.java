//package vector.StockManagement.config;
//
//
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.support.TransactionSynchronization;
//
//@Component
//public class TenantTransactionSynchronization implements TransactionSynchronization {
//
//    private final HibernateTenantFilterConfiguration tenantFilterConfig;
//
//    public TenantTransactionSynchronization(HibernateTenantFilterConfiguration tenantFilterConfig) {
//        this.tenantFilterConfig = tenantFilterConfig;
//    }
//
//    @Override
//    public void beforeCommit(boolean readOnly) {
//        tenantFilterConfig.enableTenantFilter();
//    }
//
//
//}
//
