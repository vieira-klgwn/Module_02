package vector.UtilityBillingMS.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vector.UtilityBillingMS.model.Tariff;
import vector.UtilityBillingMS.model.dto.TariffRequest;
import vector.UtilityBillingMS.services.TariffService;

import java.util.List;

@RestController
@RequestMapping("/api/tariffs")
@RequiredArgsConstructor
public class TariffController {

    private final TariffService tariffService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Tariff> create(@Valid @RequestBody TariffRequest request) {
        return new ResponseEntity<>(tariffService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<List<Tariff>> findAll() {
        return ResponseEntity.ok(tariffService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    public ResponseEntity<Tariff> findById(@PathVariable Long id) {
        return ResponseEntity.ok(tariffService.findById(id));
    }
}
