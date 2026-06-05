package vector.UtilityBillingMS.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vector.UtilityBillingMS.model.enums.EntityStatus;
import vector.UtilityBillingMS.model.enums.MeterType;

import java.time.LocalDate;

@Data
public class MeterDTO {
    @NotBlank(message = "Meter number is required")
    private String meterNumber;

    @NotNull(message = "Meter type is required")
    private MeterType type;

    @NotNull(message = "Installation date is required")
    private LocalDate installationDate;

    private EntityStatus status;

    @NotNull(message = "Customer ID is required")
    private Long customerId;
}
