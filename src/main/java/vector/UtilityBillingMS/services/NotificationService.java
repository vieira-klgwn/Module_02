package vector.UtilityBillingMS.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vector.UtilityBillingMS.model.Bill;
import vector.UtilityBillingMS.model.Customer;
import vector.UtilityBillingMS.model.Notification;
import vector.UtilityBillingMS.model.enums.NotificationStatus;
import vector.UtilityBillingMS.repositories.NotificationRepository;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification createBillNotification(Customer customer, Bill bill) {
        String monthYear = Month.of(bill.getBillingMonth())
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH) + "/" + bill.getBillingYear();
        String message = String.format(
                "Dear %s%n%nYour %s utility bill of %s FRW has been successfully processed",
                customer.getFullName(),
                monthYear,
                bill.getTotalAmount().stripTrailingZeros().toPlainString()
        );

        Notification notification = Notification.builder()
                .customer(customer)
                .subject("Utility Bill - " + monthYear)
                .message(message)
                .status(NotificationStatus.SENT)
                .referenceType("BILL")
                .referenceId(bill.getId())
                .build();
        return notificationRepository.save(notification);
    }

    public Notification createPaymentNotification(Customer customer, Bill bill, BigDecimal amountPaid) {
        String monthYear = Month.of(bill.getBillingMonth())
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH) + "/" + bill.getBillingYear();
        String message = String.format(
                "Dear %s%n%nYour payment of %s FRW for %s utility bill has been received. Remaining balance: %s FRW",
                customer.getFullName(),
                amountPaid.stripTrailingZeros().toPlainString(),
                monthYear,
                bill.getRemainingBalance().stripTrailingZeros().toPlainString()
        );

        Notification notification = Notification.builder()
                .customer(customer)
                .subject("Payment Received - " + monthYear)
                .message(message)
                .status(NotificationStatus.SENT)
                .referenceType("PAYMENT")
                .referenceId(bill.getId())
                .build();
        return notificationRepository.save(notification);
    }

    public List<Notification> findByCustomerId(Long customerId) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }
}
