package vector.UtilityBillingMS.model.dto;

import lombok.Builder;
import lombok.Data;
import vector.UtilityBillingMS.model.enums.Role;
import vector.UtilityBillingMS.model.enums.UserStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
}
