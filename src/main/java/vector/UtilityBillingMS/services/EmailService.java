package vector.UtilityBillingMS.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import vector.UtilityBillingMS.exceptions.BusinessException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromAddress;

    @Value("${app.notification.test-recipient:klgwnboy@gmail.com}")
    private String testRecipient;

    public void sendNotificationEmail(String subject, String plainTextMessage) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(testRecipient);
            helper.setSubject(subject);
            helper.setText(formatHtmlBody(plainTextMessage), true);
            mailSender.send(mimeMessage);
            logger.info("Notification email sent to {} | subject: {}", testRecipient, subject);
        } catch (MessagingException e) {
            logger.error("Failed to send notification email to {}: {}", testRecipient, e.getMessage());
            throw new BusinessException("Failed to send notification email: " + e.getMessage());
        }
    }

    public String getTestRecipient() {
        return testRecipient;
    }

    private String formatHtmlBody(String plainText) {
        String escaped = plainText
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333; line-height: 1.6;">
                    <p>%s</p>
                </body>
                </html>
                """.formatted(escaped.replace("\n", "<br/>"));
    }
}
