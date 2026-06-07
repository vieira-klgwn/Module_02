package vector.UtilityBillingMS.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import vector.UtilityBillingMS.validation.ValidPassword;

@Data
@Builder
public class ChangePasswordRequest {
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @ValidPassword
    private String newPassword;

    @NotBlank(message = "Confirmation password is required")
    private String confirmationPassword;
}
