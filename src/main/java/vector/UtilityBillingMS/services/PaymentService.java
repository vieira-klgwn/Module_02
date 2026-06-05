package vector.UtilityBillingMS.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vector.UtilityBillingMS.exceptions.BusinessException;
import vector.UtilityBillingMS.model.Bill;
import vector.UtilityBillingMS.model.Payment;
import vector.UtilityBillingMS.model.User;
import vector.UtilityBillingMS.model.dto.PaymentRequest;
import vector.UtilityBillingMS.model.enums.BillStatus;
import vector.UtilityBillingMS.repositories.BillRepository;
import vector.UtilityBillingMS.repositories.PaymentRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final BillService billService;
    private final NotificationService notificationService;

    @Transactional
    public Payment recordPayment(PaymentRequest request, User financeUser) {
        Bill bill = billService.findByReference(request.getBillReference());

        if (bill.getStatus() == BillStatus.PAID) {
            throw new BusinessException("Bill is already fully paid");
        }
        if (bill.getStatus() == BillStatus.CANCELLED) {
            throw new BusinessException("Cannot pay a cancelled bill");
        }
        if (bill.getStatus() == BillStatus.PENDING) {
            throw new BusinessException("Bill must be approved before payment");
        }

        if (request.getAmountPaid().compareTo(bill.getRemainingBalance()) > 0) {
            throw new BusinessException("Payment amount exceeds remaining balance");
        }
        if (request.getAmountPaid().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Payment amount must be positive");
        }

        Payment payment = Payment.builder()
                .bill(bill)
                .billReference(bill.getBillReference())
                .amountPaid(request.getAmountPaid())
                .paymentMethod(request.getPaymentMethod())
                .paymentDate(request.getPaymentDate())
                .recordedBy(financeUser)
                .build();
        paymentRepository.save(payment);

        BigDecimal newBalance = bill.getRemainingBalance().subtract(request.getAmountPaid());
        bill.setRemainingBalance(newBalance);

        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            bill.setStatus(BillStatus.PAID);
        } else {
            bill.setStatus(BillStatus.PARTIALLY_PAID);
        }

        billRepository.save(bill);
        notificationService.createPaymentNotification(bill.getCustomer(), bill, request.getAmountPaid());
        return payment;
    }

    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    public List<Payment> findByBillReference(String billReference) {
        return paymentRepository.findByBillReference(billReference);
    }

    public Payment findById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Payment not found with id: " + id));
    }
}
