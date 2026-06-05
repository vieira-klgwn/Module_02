package vector.UtilityBillingMS.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vector.UtilityBillingMS.model.Token;
import vector.UtilityBillingMS.model.enums.Role;
import vector.UtilityBillingMS.repositories.TokenRepository;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    private final AuthenticationService authenticationService;
    private final TokenRepository tokenRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.debug("Register request for email: {}", registerRequest.getEmail());
        registerRequest.setRole(Role.CUSTOMER);
        return ResponseEntity.ok(authenticationService.register(registerRequest));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<AuthenticationResponse> registerAdmin(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.debug("Register request for email: {}", registerRequest.getEmail());
        registerRequest.setRole(Role.ADMIN);
        return ResponseEntity.ok(authenticationService.register(registerRequest));
    }

    @PostMapping("/register/operator")
    public ResponseEntity<AuthenticationResponse> registerOperator(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.debug("Register request for email: {}", registerRequest.getEmail());
        registerRequest.setRole(Role.OPERATOR);
        return ResponseEntity.ok(authenticationService.register(registerRequest));
    }

    @PostMapping("/register/finance")
    public ResponseEntity<AuthenticationResponse> registerFinance(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.debug("Register request for email: {}", registerRequest.getEmail());
        registerRequest.setRole(Role.FINANCE);
        return ResponseEntity.ok(authenticationService.register(registerRequest));
    }



    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        logger.debug("Login request for email: {}", request.getEmail());
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletResponse response, HttpServletRequest request) throws IOException {
        authenticationService.refreshToken(request, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Optional<Token> tokenEntity = tokenRepository.findByToken(token);
            tokenEntity.ifPresent(dbToken -> {
                dbToken.setExpired(true);
                dbToken.setRevoked(true);
                tokenRepository.save(dbToken);
            });
        }
        return ResponseEntity.ok().build();
    }
}
