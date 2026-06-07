package vector.UtilityBillingMS.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vector.UtilityBillingMS.model.Bill;
import vector.UtilityBillingMS.model.Customer;
import vector.UtilityBillingMS.model.Notification;
import vector.UtilityBillingMS.model.User;
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
    private final EmailService emailService;
    private final CustomerAccessService customerAccessService;

    public Notification createBillNotification(Customer customer, Bill bill) {
        String monthYear = formatMonthYear(bill.getBillingMonth(), bill.getBillingYear());
        String message = buildBillProcessedMessage(customer.getFullName(), monthYear, bill.getTotalAmount());

        return saveAndEmail(customer, bill.getId(), "BILL",
                "Utility Bill - " + monthYear, message);
    }

    public Notification createPaymentNotification(Customer customer, Bill bill, BigDecimal amountPaid, boolean fullyPaid) {
        String monthYear = formatMonthYear(bill.getBillingMonth(), bill.getBillingYear());
        String message;
        String subject;

        if (fullyPaid) {
            message = buildBillProcessedMessage(customer.getFullName(), monthYear, bill.getTotalAmount());
            subject = "Bill Paid - " + monthYear;
        } else {
            message = String.format(
                    "Dear %s%n%nYour payment of %s FRW for %s utility bill has been received. Remaining balance: %s FRW",
                    customer.getFullName(),
                    amountPaid.stripTrailingZeros().toPlainString(),
                    monthYear,
                    bill.getRemainingBalance().stripTrailingZeros().toPlainString()
            );
            subject = "Payment Received - " + monthYear;
        }

        return saveAndEmail(customer, bill.getId(), "PAYMENT", subject, message);
    }

    public String buildBillProcessedMessage(String customerName, String monthYear, BigDecimal amount) {
        return String.format(
                "Dear %s%n%nYour %s utility bill of %s FRW has been successfully processed",
                customerName,
                monthYear,
                amount.stripTrailingZeros().toPlainString()
        );
    }

    private Notification saveAndEmail(Customer customer, Long referenceId, String referenceType,
                                      String subject, String message) {
        Notification notification = Notification.builder()
                .customer(customer)
                .subject(subject)
                .message(message)
                .status(NotificationStatus.PENDING)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .recipientEmail(emailService.getTestRecipient())
                .build();

        Notification saved = notificationRepository.save(notification);

        boolean emailSent = emailService.sendNotificationEmail(subject, message);
        saved.setStatus(emailSent ? NotificationStatus.SENT : NotificationStatus.FAILED);
        return notificationRepository.save(saved);
    }

    private String formatMonthYear(int month, int year) {
        return Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + "/" + year;
    }

    public List<Notification> findByCustomerId(Long customerId) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public List<Notification> findByCustomerIdForUser(Long customerId, User user) {
        customerAccessService.ensureOwnCustomer(user, customerId);
        return findByCustomerId(customerId);
    }

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }
}
