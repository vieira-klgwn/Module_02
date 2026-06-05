package vector.UtilityBillingMS.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vector.UtilityBillingMS.model.Payment;
import vector.UtilityBillingMS.model.User;
import vector.UtilityBillingMS.model.dto.PaymentRequest;
import vector.UtilityBillingMS.services.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('FINANCE')")
    public ResponseEntity<Payment> recordPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal User financeUser) {
        return new ResponseEntity<>(paymentService.recordPayment(request, financeUser), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    public ResponseEntity<List<Payment>> findAll() {
        return ResponseEntity.ok(paymentService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    public ResponseEntity<Payment> findById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @GetMapping("/bill/{billReference}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    public ResponseEntity<List<Payment>> findByBillReference(@PathVariable String billReference) {
        return ResponseEntity.ok(paymentService.findByBillReference(billReference));
    }
}
