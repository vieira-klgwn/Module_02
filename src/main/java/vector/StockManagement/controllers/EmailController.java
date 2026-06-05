package vector.StockManagement.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vector.StockManagement.model.dto.EmailRequest;
import vector.StockManagement.services.EmailService;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;

    @PostMapping("/simple")
    public ResponseEntity<String> sendSimpleEmail(@Valid @RequestBody EmailRequest request) {
        emailService.sendSimpleEmail(request.getTo(), request.getSubject(), request.getBody());
        return ResponseEntity.ok("Simple email sent");
    }

    @PostMapping("/html")
    public ResponseEntity<String> sendHtmlEmail(@Valid @RequestBody EmailRequest request) {
        emailService.sendHtmlEmail(request.getTo(), request.getSubject(), request.getBody());
        return ResponseEntity.ok("HTML email sent");
    }
}
