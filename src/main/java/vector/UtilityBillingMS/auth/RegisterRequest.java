package vector.UtilityBillingMS.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vector.UtilityBillingMS.model.enums.Role;
import vector.UtilityBillingMS.validation.RwandaPhone;
import vector.UtilityBillingMS.validation.ValidNationalId;
import vector.UtilityBillingMS.validation.ValidPassword;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone number is required")
    @RwandaPhone
    private String phoneNumber;

    @NotBlank(message = "National ID is required")
    @ValidNationalId
    private String nationalId;

    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;

    private Role role;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
