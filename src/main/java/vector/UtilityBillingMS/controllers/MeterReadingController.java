package vector.UtilityBillingMS.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vector.UtilityBillingMS.model.MeterReading;
import vector.UtilityBillingMS.model.User;
import vector.UtilityBillingMS.model.dto.MeterReadingRequest;
import vector.UtilityBillingMS.services.MeterReadingService;

import java.util.List;

@RestController
@RequestMapping("/api/meter-readings")
@RequiredArgsConstructor
public class MeterReadingController {

    private final MeterReadingService meterReadingService;

    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<MeterReading> create(
            @Valid @RequestBody MeterReadingRequest request,
            @AuthenticationPrincipal User operator) {
        return new ResponseEntity<>(meterReadingService.create(request, operator), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR')")
    public ResponseEntity<List<MeterReading>> findAll() {
        return ResponseEntity.ok(meterReadingService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR')")
    public ResponseEntity<MeterReading> findById(@PathVariable Long id) {
        return ResponseEntity.ok(meterReadingService.findById(id));
    }

    @GetMapping("/meter/{meterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'OPERATOR', 'CUSTOMER')")
    public ResponseEntity<List<MeterReading>> findByMeter(@PathVariable Long meterId) {
        return ResponseEntity.ok(meterReadingService.findByMeterId(meterId));
    }
}
