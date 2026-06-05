package vector.UtilityBillingMS.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vector.UtilityBillingMS.exceptions.BusinessException;
import vector.UtilityBillingMS.model.*;
import vector.UtilityBillingMS.model.dto.BillGenerateRequest;
import vector.UtilityBillingMS.model.enums.BillStatus;
import vector.UtilityBillingMS.repositories.BillRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final MeterReadingService meterReadingService;
    private final TariffService tariffService;
    private final MeterService meterService;
    private final NotificationService notificationService;

    @Transactional
    public Bill generateBill(BillGenerateRequest request) {
        MeterReading reading = meterReadingService.findById(request.getMeterReadingId());
        if (billRepository.existsByMeterReadingId(reading.getId())) {
            throw new BusinessException("Bill already exists for this meter reading");
        }

        Meter meter = reading.getMeter();
        meterService.ensureActive(meter);
        Customer customer = meter.getCustomer();

        Tariff tariff = tariffService.findApplicableTariff(meter.getType(), reading.getReadingDate());
        BigDecimal consumption = reading.getCurrentReading().subtract(reading.getPreviousReading());

        BigDecimal subtotal = tariffService.calculateConsumptionCost(tariff, consumption);
        BigDecimal fixedCharge = tariff.getFixedServiceCharge();
        BigDecimal penaltyAmount = BigDecimal.ZERO;
        if (request.isApplyPenalty() && tariff.getPenaltyRate().compareTo(BigDecimal.ZERO) > 0) {
            penaltyAmount = subtotal.multiply(tariff.getPenaltyRate())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        BigDecimal beforeVat = subtotal.add(fixedCharge).add(penaltyAmount);
        BigDecimal vatAmount = beforeVat.multiply(tariff.getVatRate())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = beforeVat.add(vatAmount);

        Bill bill = Bill.builder()
                .billReference("BILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customer(customer)
                .meter(meter)
                .meterReading(reading)
                .tariff(tariff)
                .billingMonth(reading.getBillingMonth())
                .billingYear(reading.getBillingYear())
                .consumption(consumption)
                .subtotal(subtotal)
                .vatAmount(vatAmount)
                .fixedCharge(fixedCharge)
                .penaltyAmount(penaltyAmount)
                .totalAmount(totalAmount)
                .remainingBalance(totalAmount)
                .status(BillStatus.PENDING)
                .build();

        Bill saved = billRepository.save(bill);
        notificationService.createBillNotification(customer, saved);
        return saved;
    }

    public Bill approveBill(Long billId, User approver) {
        Bill bill = findById(billId);
        if (bill.getStatus() != BillStatus.PENDING) {
            throw new BusinessException("Only pending bills can be approved");
        }
        bill.setStatus(BillStatus.APPROVED);
        bill.setApprovedBy(approver);
        bill.setApprovedAt(java.time.LocalDateTime.now());
        return billRepository.save(bill);
    }

    public List<Bill> findAll() {
        return billRepository.findAll();
    }

    public Bill findById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Bill not found with id: " + id));
    }

    public Bill findByReference(String reference) {
        return billRepository.findByBillReference(reference)
                .orElseThrow(() -> new BusinessException("Bill not found with reference: " + reference));
    }

    public List<Bill> findByCustomerId(Long customerId) {
        return billRepository.findByCustomerId(customerId);
    }
}
