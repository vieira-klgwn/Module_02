package vector.UtilityBillingMS.model.dto;

import lombok.Builder;
import lombok.Data;
import vector.UtilityBillingMS.model.enums.EntityStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomerResponseDTO {
    private Long id;
    private String fullName;
    private String nationalId;
    private String phoneNumber;
    private EntityStatus status;
    private LocalDateTime createdAt;
}
