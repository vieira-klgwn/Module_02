package vector.UtilityBillingMS.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TariffTierRequest {
    @NotNull(message = "Minimum consumption is required")
    @Positive(message = "Minimum consumption must be positive")
    private BigDecimal minConsumption;

    private BigDecimal maxConsumption;

    @NotNull(message = "Rate per unit is required")
    @Positive(message = "Rate per unit must be positive")
    private BigDecimal ratePerUnit;
}
