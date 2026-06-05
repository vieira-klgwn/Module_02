package vector.UtilityBillingMS.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vector.UtilityBillingMS.model.enums.Role;
import vector.UtilityBillingMS.model.enums.UserStatus;
import vector.UtilityBillingMS.validation.RwandaPhone;

@Data
public class UserDTO {
    private Long id;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone number is required")
    @RwandaPhone
    private String phoneNumber;

    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    private UserStatus status;
}
