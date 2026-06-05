package vector.UtilityBillingMS.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vector.UtilityBillingMS.model.Meter;
import vector.UtilityBillingMS.model.dto.MeterRequest;
import vector.UtilityBillingMS.services.MeterService;

import java.util.List;

@RestController
@RequestMapping("/api/meters")
@RequiredArgsConstructor
public class MeterController {

    private final MeterService meterService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<Meter> create(@Valid @RequestBody MeterRequest request) {
        return new ResponseEntity<>(meterService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR')")
    public ResponseEntity<List<Meter>> findAll() {
        return ResponseEntity.ok(meterService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR', 'CUSTOMER')")
    public ResponseEntity<Meter> findById(@PathVariable Long id) {
        return ResponseEntity.ok(meterService.findById(id));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR', 'CUSTOMER')")
    public ResponseEntity<List<Meter>> findByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(meterService.findByCustomerId(customerId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Meter> update(@PathVariable Long id, @Valid @RequestBody MeterRequest request) {
        return ResponseEntity.ok(meterService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        meterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
