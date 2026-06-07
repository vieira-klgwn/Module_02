package vector.UtilityBillingMS.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vector.UtilityBillingMS.model.Tariff;
import vector.UtilityBillingMS.model.User;
import vector.UtilityBillingMS.model.enums.MeterType;
import vector.UtilityBillingMS.model.enums.Role;
import vector.UtilityBillingMS.model.enums.TariffType;
import vector.UtilityBillingMS.model.enums.UserStatus;
import vector.UtilityBillingMS.repositories.TariffRepository;
import vector.UtilityBillingMS.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TariffRepository tariffRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createUserIfMissing("admin@wasac.rw", "System Admin", "0788000001", "1199880012345601", Role.ADMIN, "Admin@1234");
        createUserIfMissing("operator@wasac.rw", "Meter Operator", "0788000002", "1199880012345602", Role.OPERATOR, "Operator@123");
        createUserIfMissing("finance@wasac.rw", "Finance Officer", "0788000003", "1199880012345603", Role.FINANCE, "Finance@123");
        seedBaselineTariffIfMissing(MeterType.WATER, "WASAC Water Baseline", new BigDecimal("350"));
        seedBaselineTariffIfMissing(MeterType.ELECTRICITY, "REG Electricity Baseline", new BigDecimal("120"));
    }

    private void createUserIfMissing(String email, String fullName, String phone, String nationalId, Role role, String password) {
        if (!userRepository.existsByEmail(email)) {
            userRepository.save(User.builder()
                    .email(email)
                    .fullName(fullName)
                    .phoneNumber(phone)
                    .nationalId(nationalId)
                    .password(passwordEncoder.encode(password))
                    .role(role)
                    .status(UserStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }

    private void seedBaselineTariffIfMissing(MeterType type, String name, BigDecimal flatRate) {
        if (tariffRepository.findApplicableTariff(type, LocalDate.now()).isPresent()) {
            return;
        }
        Tariff tariff = Tariff.builder()
                .name(name)
                .utilityType(type)
                .tariffType(TariffType.FLAT)
                .version(1)
                .effectiveFrom(LocalDate.of(2020, 1, 1))
                .vatRate(new BigDecimal("18"))
                .fixedServiceCharge(type == MeterType.WATER ? new BigDecimal("500") : new BigDecimal("1000"))
                .penaltyRate(BigDecimal.ZERO)
                .flatRate(flatRate)
                .build();
        tariffRepository.save(tariff);
    }
}
