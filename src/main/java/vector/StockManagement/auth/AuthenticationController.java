package vector.StockManagement.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vector.StockManagement.config.TenantContext;
import vector.StockManagement.model.Tenant;
import vector.StockManagement.model.Token;
import vector.StockManagement.model.User;
import vector.StockManagement.model.enums.ActivityCategory;
import vector.StockManagement.model.enums.Role;
import vector.StockManagement.repositories.TenantRepository;
import vector.StockManagement.repositories.TokenRepository;
import vector.StockManagement.services.ActivityService;
import vector.StockManagement.services.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final TokenRepository tokenRepository;
    private final TenantRepository tenantRepository;
    private final ActivityService activityService;


    @Value("${app.upload.dir:uploads}")
    private String uploadDir;


    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest registerRequest, @AuthenticationPrincipal User user) {


        String regex = "^\\+2507[8293]\\d{7}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(registerRequest.getPhone());
        if (!matcher.matches()) {
            logger.warn("Phone number is invalid");
            return ResponseEntity.badRequest().build();
        }



        logger.debug("Register request for email: {}", registerRequest.getEmail());
        activityService.createActivity(user, "Created User", ActivityCategory.USER_REGISTRATION, "User with email " + registerRequest.getEmail() + " created");
        return ResponseEntity.ok(authenticationService.register(registerRequest));
    }

    @PostMapping("/register/retailer")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR')")
    public ResponseEntity<AuthenticationResponse> registerRetailer(@Valid @RequestBody RegisterRequest registerRequest, @AuthenticationPrincipal User currentDistributor) {
//        registerRequest.setRole(Role.RETAILER);
        registerRequest.setDistributor_id(currentDistributor.getId());
        return ResponseEntity.ok(authenticationService.register(registerRequest));
    }

    @PostMapping("/register/whole_saler")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR')")
    public ResponseEntity<AuthenticationResponse> registerWholeSaler(@Valid @RequestBody RegisterRequest registerRequest, @AuthenticationPrincipal User currentDistributor) {
//        registerRequest.setRole(Role.WHOLE_SALER);
        registerRequest.setDistributor_id(currentDistributor.getId());
        return ResponseEntity.ok(authenticationService.register(registerRequest));
    }

    @PostMapping("/register/accountant")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<AuthenticationResponse> registerAccountantAtStore(@Valid @RequestBody RegisterRequest registerRequest, @AuthenticationPrincipal User currentDistributor) {
//        registerRequest.setRole(Role.ACCOUNTANT);
        registerRequest.setDistributor_id(currentDistributor.getId());
        return ResponseEntity.ok(authenticationService.register(registerRequest));
    }


    @PostMapping("register/store_manager")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR')")
    public ResponseEntity<AuthenticationResponse> registerStoreManager(@Valid @RequestBody RegisterRequest registerRequest, @AuthenticationPrincipal User currentDistributor) {
//        registerRequest.setRole(Role.STORE_MANAGER);
        registerRequest.setDistributor_id(currentDistributor.getId());

        Tenant tenant = tenantRepository.findById(TenantContext.getTenantId()).orElseThrow(() -> new IllegalStateException("Tenant not found"));
        return ResponseEntity.ok(authenticationService.register(registerRequest));
    }

    @PostMapping("register/sales_manager")
    @PreAuthorize("hasAnyRole('DISTRIBUTOR')")
    public ResponseEntity<AuthenticationResponse> registerSalesManager(@Valid @RequestBody RegisterRequest registerRequest, @AuthenticationPrincipal User currentDistributor) {
//        registerRequest.setRole(Role.SALES_MANAGER);
        registerRequest.setDistributor_id(TenantContext.getTenantId());

        Tenant tenant = tenantRepository.findById(TenantContext.getTenantId()).orElseThrow(() -> new IllegalStateException("Tenant not found"));
        return ResponseEntity.ok(authenticationService.register(registerRequest));
    }

    @PostMapping("/register/md")
    public ResponseEntity<AuthenticationResponse> registerManagingDirector(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authenticationService.createManagingDirector(registerRequest));
    }

    @PostMapping("/register/super")
    public ResponseEntity<AuthenticationResponse> registerSuperUser(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authenticationService.createSuperAdmin(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest authenticationRequest) {
        logger.debug("Login request for email: {}", authenticationRequest.getEmail());
        return ResponseEntity.ok(authenticationService.authenticate(authenticationRequest));
    }


    @PostMapping("/super_user/login")
    public ResponseEntity<AuthenticationResponse> superUserLogin(@RequestBody AuthenticationRequest authenticationRequest) {
        return ResponseEntity.ok(authenticationService.authenticate(authenticationRequest));
    }

    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletResponse response, HttpServletRequest request) throws IOException {
        logger.debug("Refresh token request received");
        try {
            authenticationService.refreshToken(request, response);
        } catch (Exception e) {
            logger.error("Failed to refresh token: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid refresh token");
        }
    }



    @PostMapping("/{id}/upload-image")
    @PreAuthorize("hasAnyRole('ADMIN','DISTRIBUTOR')")
    public ResponseEntity<User> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        User user = userService.getUserById(id).orElseThrow(()-> new IllegalStateException("User with id " + id + " not found"));
        if (user == null) return ResponseEntity.notFound().build();

        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String filename = UUID.randomUUID() + ext;
        Path target = dir.resolve(filename);
        file.transferTo(target.toFile());

        user.setImageUrl("/" + uploadDir + "/" + filename);
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        logger.debug("Logout request with Authorization header: {}", authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Optional<Token> tokenEntity = tokenRepository.findByToken(token);
            if (tokenEntity.isPresent()) {
                Token dbToken = tokenEntity.get();
                dbToken.setExpired(true);
                dbToken.setRevoked(true);
                tokenRepository.save(dbToken);
                logger.info("Token invalidated for user: {}", dbToken.getUser().getEmail());
            } else {
                logger.warn("Token not found in database: {}", token);
            }
        } else {
            logger.warn("No valid Bearer token provided for logout");
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping({"/forgot-password", "/request-password-reset"})
    public ResponseEntity<String> requestPasswordReset(@RequestBody ForgotPasswordRequest request) {
        logger.debug("Password reset request for email: {}", request.getEmail());
        try {
            authenticationService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok("Password reset email sent successfully");
        } catch (Exception e) {
            logger.error("Failed to process password reset: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to send password reset email: " + e.getMessage());
        }
    }

    @GetMapping("/reset-password")  // New: Verify token on link click (for frontend to check before showing form)
    public ResponseEntity<String> verifyResetToken(@RequestParam("te") String token) {
        logger.debug("Token verification request for: {}", token);
        try {
            authenticationService.verifyResetToken(token);
            return ResponseEntity.ok("Token is valid. Proceed with password reset.");
        } catch (Exception e) {
            logger.error("Token verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid or expired reset token");
        }
    }



    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        logger.debug("Password reset attempt with token: {}", request.getResetToken());
        try {
            authenticationService.resetPassword(request.getResetToken(), request.getNewPassword());
            return ResponseEntity.ok("Password reset successfully");
        } catch (Exception e) {
            logger.error("Failed to reset password: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to reset password: " + e.getMessage()); //make sure in production,you'll remove this e.getMessage()
        }
    }
}