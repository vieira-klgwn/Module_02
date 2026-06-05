package vector.UtilityBillingMS.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Value("${app.notification.test-recipient:klgwnboy@gmail.com}")
    private String testRecipient;

    /**
     * Sends notification email. Returns true on success, false on failure (never throws).
     */
    public boolean sendNotificationEmail(String subject, String plainTextMessage) {
        return sendHtmlEmail(testRecipient, subject, formatHtmlBody(plainTextMessage));
    }

    public boolean sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            logger.info("Email sent to {} | subject: {}", to, subject);
            return true;
        } catch (MessagingException | MailException e) {
            logger.error("Failed to send email to {} | subject: {} | error: {}", to, subject, e.getMessage());
            return false;
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
