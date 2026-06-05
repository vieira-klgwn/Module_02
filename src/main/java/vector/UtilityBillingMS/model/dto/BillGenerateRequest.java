package vector.UtilityBillingMS.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BillGenerateRequest {
    @NotNull(message = "Meter reading ID is required")
    private Long meterReadingId;

    private boolean applyPenalty;
}
