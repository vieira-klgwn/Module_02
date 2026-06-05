package vector.StockManagement.controllers;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vector.StockManagement.config.TenantContext;
import vector.StockManagement.exceptions.TenantException;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestErrorController {

    private static final Logger logger = LoggerFactory.getLogger(TestErrorController.class);

    @GetMapping("/access-denied")
    @PreAuthorize("hasRole('NONEXISTENT_ROLE')")
    public ResponseEntity<String> testAccessDenied() {
        return ResponseEntity.ok("This should not be reached");
    }

    @GetMapping("/tenant-error")
    public ResponseEntity<String> testTenantError() {
        Long tenantId = TenantContext.getTenantId();
        logger.info("Testing tenant error for tenant: {}", tenantId);
        
        throw new TenantException("Test tenant exception", tenantId, "test-operation");
    }

    @GetMapping("/generic-error")
    public ResponseEntity<String> testGenericError() {
        Long tenantId = TenantContext.getTenantId();
        logger.info("Testing generic error for tenant: {}", tenantId);
        
        throw new RuntimeException("Test generic exception");
    }

    @GetMapping("/validation-error")
    public ResponseEntity<String> testValidationError(@RequestParam(required = false) String requiredParam) {
        Long tenantId = TenantContext.getTenantId();
        logger.info("Testing validation error for tenant: {}", tenantId);
        
        if (requiredParam == null || requiredParam.trim().isEmpty()) {
            throw new IllegalArgumentException("Required parameter 'requiredParam' is missing or empty");
        }
        
        return ResponseEntity.ok("Validation passed");
    }

    @GetMapping("/tenant-info")
    public ResponseEntity<Map<String, Object>> getTenantInfo() {
        Long tenantId = TenantContext.getTenantId();
        logger.info("Getting tenant info for tenant: {}", tenantId);
        
        return ResponseEntity.ok(Map.of(
            "tenantId", tenantId != null ? tenantId : "null",
            "message", "Current tenant context information"
        ));
    }
}
