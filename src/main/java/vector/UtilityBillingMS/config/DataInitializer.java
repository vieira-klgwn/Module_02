package vector.UtilityBillingMS.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vector.UtilityBillingMS.model.User;
import vector.UtilityBillingMS.model.enums.Role;
import vector.UtilityBillingMS.model.enums.UserStatus;
import vector.UtilityBillingMS.repositories.UserRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createUserIfMissing("admin@wasac.rw", "System Admin", "0788000001", Role.ADMIN, "Admin@1234");
        createUserIfMissing("operator@wasac.rw", "Meter Operator", "0788000002", Role.OPERATOR, "Operator@123");
        createUserIfMissing("finance@wasac.rw", "Finance Officer", "0788000003", Role.FINANCE, "Finance@123");
    }

    private void createUserIfMissing(String email, String fullName, String phone, Role role, String password) {
        if (!userRepository.existsByEmail(email)) {
            userRepository.save(User.builder()
                    .email(email)
                    .fullName(fullName)
                    .phoneNumber(phone)
                    .password(passwordEncoder.encode(password))
                    .role(role)
                    .status(UserStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }
}
