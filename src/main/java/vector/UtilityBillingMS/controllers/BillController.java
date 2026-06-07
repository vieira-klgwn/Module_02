package vector.UtilityBillingMS.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vector.UtilityBillingMS.model.Bill;
import vector.UtilityBillingMS.model.User;
import vector.UtilityBillingMS.model.dto.BillGenerateRequest;
import vector.UtilityBillingMS.services.BillService;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<Bill> generate(@Valid @RequestBody BillGenerateRequest request) {
        return new ResponseEntity<>(billService.generateBill(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<Bill> approve(@PathVariable Long id, @AuthenticationPrincipal User approver) {
        return ResponseEntity.ok(billService.approveBill(id, approver));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    public ResponseEntity<List<Bill>> findAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(billService.findAllForUser(user));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    public ResponseEntity<Bill> findById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(billService.findByIdForUser(id, user));
    }

    @GetMapping("/reference/{reference}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    public ResponseEntity<Bill> findByReference(@PathVariable String reference, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(billService.findByReferenceForUser(reference, user));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    public ResponseEntity<List<Bill>> findByCustomer(
            @PathVariable Long customerId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(billService.findByCustomerIdForUser(customerId, user));
    }
}
