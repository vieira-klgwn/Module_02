package vector.StockManagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vector.StockManagement.model.enums.NotificationChannel;
import vector.StockManagement.model.enums.NotificationStatus;
import vector.StockManagement.model.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Notification Entity
@Entity
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Notification extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private NotificationType type;

    private String title;




    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "channel")
    private NotificationChannel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    @JsonIgnore
    private User recipient;

    @Size(max = 200)
    @Column(name = "recipient_address")
    private String recipientAddress; // email or phone number

    @NotBlank
    @Size(max = 200)
    @Column(name = "subject")
    private String subject;

    @Column(name = "message", length = 1000)
    private String message;


    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "json")
    private Map<String, Object> payload = new HashMap<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Size(max = 500)
    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @JsonIgnore
    private Tenant tenant;

    @Size(max = 100)
    @Column(name = "reference_type")
    private String referenceType; // e.g., "ORDER", "INVOICE"

    @Column(name = "reference_id")
    private Long referenceId;



    public Notification(NotificationType type, NotificationChannel channel,
                        String subject, String message, Tenant tenant) {
        this.type = type;
        this.channel = channel;
        this.subject = subject;
        this.message = message;
        this.tenant = tenant;
    }


    // Helper methods
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsDelivered() {
        this.status = NotificationStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
        this.retryCount++;

        // Calculate next retry time (exponential backoff)
        long delayMinutes = Math.min(60, (long) Math.pow(2, retryCount - 1) * 5);
        this.nextRetryAt = LocalDateTime.now().plusMinutes(delayMinutes);
    }

    public boolean canRetry() {
        return status == NotificationStatus.FAILED &&
                retryCount < 5 &&
                LocalDateTime.now().isAfter(nextRetryAt);
    }

    public void setReference(String referenceType, Long referenceId) {
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }

    @Override
    public String toString() {
        return "Notification{" +
                ", type=" + type +
                ", channel=" + channel +
                ", status=" + status +
                ", subject='" + subject + '\'' +
                ", retryCount=" + retryCount +
                '}';
    }
}