package vector.UtilityBillingMS.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import vector.UtilityBillingMS.model.enums.EntityStatus;
import vector.UtilityBillingMS.validation.RwandaPhone;
import vector.UtilityBillingMS.validation.ValidNationalId;

@Data
public class CustomerDTO {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "National ID is required")
    @ValidNationalId
    private String nationalId;

    @NotBlank(message = "Phone number is required")
    @RwandaPhone
    private String phoneNumber;

    private EntityStatus status;
}
