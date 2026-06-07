package vector.UtilityBillingMS.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vector.UtilityBillingMS.config.JwtService;
import vector.UtilityBillingMS.exceptions.BusinessException;
import vector.UtilityBillingMS.model.Customer;
import vector.UtilityBillingMS.model.Token;
import vector.UtilityBillingMS.model.User;
import vector.UtilityBillingMS.model.enums.Role;
import vector.UtilityBillingMS.model.enums.TokenType;
import vector.UtilityBillingMS.model.enums.UserStatus;
import vector.UtilityBillingMS.repositories.CustomerRepository;
import vector.UtilityBillingMS.repositories.TokenRepository;
import vector.UtilityBillingMS.repositories.UserRepository;
import vector.UtilityBillingMS.services.NationalIdValidationService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final Logger logger = Logger.getLogger(AuthenticationService.class.getName());
    private final CustomerRepository customerRepository;
    private final NationalIdValidationService nationalIdValidationService;

    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already taken: " + request.getEmail());
        }
        nationalIdValidationService.ensureUnique(request.getNationalId());
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match");
        }

        var user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .nationalId(request.getNationalId())
                .role(request.getRole() != null ? request.getRole() : Role.CUSTOMER)
                .createdAt(LocalDateTime.now())
                .build();
        var savedUser = userRepository.save(user);

        var customer = new Customer();
        customer.setUser(savedUser);
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setNationalId(request.getNationalId());
        customer.setFullName(request.getFullName());
        customer.setAddress("Not provided");
        customerRepository.saveAndFlush(customer);
        savedUser.setCustomer(customer);


        var token = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, token);
        saveUserToken(savedUser, refreshToken);

        return AuthenticationResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getPassword()));
        var user = userRepository.findByEmail(authenticationRequest.getEmail())
                .orElseThrow(() -> new BusinessException("User not found: " + authenticationRequest.getEmail()));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("User account is inactive");
        }
        var token = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, refreshToken);
        saveUserToken(user, token);
        return AuthenticationResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();
    }

    private void revokeAllUserTokens(User user) {
        var validTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validTokens.isEmpty()) {
            return;
        }
        validTokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });
        tokenRepository.saveAll(validTokens);
    }

    private void saveUserToken(User savedUser, String jwtToken) {
        var token = Token.builder()
                .token(jwtToken)
                .user(savedUser)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .createdDate(LocalDateTime.now())
                .build();
        tokenRepository.save(token);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or Invalid Authorization header");
            return;
        }

        String refreshToken = authHeader.substring(7);
        String email = jwtService.extractUsername(refreshToken);

        if (email == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Refresh Token");
            return;
        }

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("User not found: " + email));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or Expired Refresh Token");
            return;
        }
        var accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);

        var authResponse = AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
    }
}
