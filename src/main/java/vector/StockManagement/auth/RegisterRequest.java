package vector.StockManagement.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.NumberFormat;
import vector.StockManagement.model.Tenant;
import vector.StockManagement.model.enums.Role;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private String confirmPassword;
    private Role role;

    private Long distributor_id;

    @NotBlank
    private String gender;
    @NotNull
    private Long tenantId;
    private Tenant tenant;
    private LocalDate birthDate;

    private String phone;
    private String nationality;
}