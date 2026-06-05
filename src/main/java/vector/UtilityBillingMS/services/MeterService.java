package vector.UtilityBillingMS.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vector.UtilityBillingMS.exceptions.BusinessException;
import vector.UtilityBillingMS.model.Customer;
import vector.UtilityBillingMS.model.Meter;
import vector.UtilityBillingMS.model.dto.MeterRequest;
import vector.UtilityBillingMS.model.enums.EntityStatus;
import vector.UtilityBillingMS.repositories.MeterRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterService {

    private final MeterRepository meterRepository;
    private final CustomerService customerService;

    public Meter create(MeterRequest request) {
        if (meterRepository.existsByMeterNumber(request.getMeterNumber())) {
            throw new BusinessException("Meter number already exists: " + request.getMeterNumber());
        }
        Customer customer = customerService.findById(request.getCustomerId());
        customerService.ensureActive(customer);

        Meter meter = Meter.builder()
                .meterNumber(request.getMeterNumber())
                .type(request.getType())
                .installationDate(request.getInstallationDate())
                .status(request.getStatus() != null ? request.getStatus() : EntityStatus.ACTIVE)
                .customer(customer)
                .build();
        return meterRepository.save(meter);
    }

    public List<Meter> findAll() {
        return meterRepository.findAll();
    }

    public Meter findById(Long id) {
        return meterRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Meter not found with id: " + id));
    }

    public List<Meter> findByCustomerId(Long customerId) {
        return meterRepository.findByCustomerId(customerId);
    }

    public Meter update(Long id, MeterRequest request) {
        Meter meter = findById(id);
        if (!meter.getMeterNumber().equals(request.getMeterNumber())
                && meterRepository.existsByMeterNumber(request.getMeterNumber())) {
            throw new BusinessException("Meter number already exists: " + request.getMeterNumber());
        }
        Customer customer = customerService.findById(request.getCustomerId());
        customerService.ensureActive(customer);

        meter.setMeterNumber(request.getMeterNumber());
        meter.setType(request.getType());
        meter.setInstallationDate(request.getInstallationDate());
        meter.setCustomer(customer);
        if (request.getStatus() != null) {
            meter.setStatus(request.getStatus());
        }
        return meterRepository.save(meter);
    }

    public void delete(Long id) {
        if (!meterRepository.existsById(id)) {
            throw new BusinessException("Meter not found with id: " + id);
        }
        meterRepository.deleteById(id);
    }

    public void ensureActive(Meter meter) {
        if (meter.getStatus() != EntityStatus.ACTIVE) {
            throw new BusinessException("Meter is inactive and cannot be used");
        }
        customerService.ensureActive(meter.getCustomer());
    }
}
