package vector.UtilityBillingMS.model.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.NumberFormat;
import vector.UtilityBillingMS.model.enums.EntityStatus;
import vector.UtilityBillingMS.model.enums.UserStatus;
import vector.UtilityBillingMS.validation.RwandaPhone;
import vector.UtilityBillingMS.validation.ValidPassword;

@Data
public class CustomerRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "National ID is required")
    private String nationalId;

    @NotBlank(message = "Address is required")
    private String address;


    private String userStatus;


    @NotBlank(message = "Phone number is required")
    @RwandaPhone
    private String phoneNumber;
    @ValidPassword
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private EntityStatus status;
}
