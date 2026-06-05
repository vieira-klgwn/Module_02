package vector.UtilityBillingMS.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vector.UtilityBillingMS.exceptions.BusinessException;
import vector.UtilityBillingMS.model.Meter;
import vector.UtilityBillingMS.model.MeterReading;
import vector.UtilityBillingMS.model.User;
import vector.UtilityBillingMS.model.dto.MeterReadingRequest;
import vector.UtilityBillingMS.repositories.MeterReadingRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterReadingService {

    private final MeterReadingRepository meterReadingRepository;
    private final MeterService meterService;

    public MeterReading create(MeterReadingRequest request, User operator) {
        Meter meter = meterService.findById(request.getMeterId());
        meterService.ensureActive(meter);

        if (request.getCurrentReading().compareTo(request.getPreviousReading()) <= 0) {
            throw new BusinessException("Current reading must be greater than previous reading");
        }
        if (request.getPreviousReading().compareTo(BigDecimal.ZERO) < 0
                || request.getCurrentReading().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Readings cannot be negative");
        }

        int month = request.getReadingDate().getMonthValue();
        int year = request.getReadingDate().getYear();
        if (meterReadingRepository.existsByMeterIdAndBillingMonthAndBillingYear(meter.getId(), month, year)) {
            throw new BusinessException("A reading already exists for this meter in " + month + "/" + year);
        }

        MeterReading reading = MeterReading.builder()
                .meter(meter)
                .previousReading(request.getPreviousReading())
                .currentReading(request.getCurrentReading())
                .readingDate(request.getReadingDate())
                .billingMonth(month)
                .billingYear(year)
                .recordedBy(operator)
                .build();
        return meterReadingRepository.save(reading);
    }

    public List<MeterReading> findAll() {
        return meterReadingRepository.findAll();
    }

    public MeterReading findById(Long id) {
        return meterReadingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Meter reading not found with id: " + id));
    }

    public List<MeterReading> findByMeterId(Long meterId) {
        meterService.findById(meterId);
        return meterReadingRepository.findAll().stream()
                .filter(r -> r.getMeter().getId().equals(meterId))
                .toList();
    }
}
