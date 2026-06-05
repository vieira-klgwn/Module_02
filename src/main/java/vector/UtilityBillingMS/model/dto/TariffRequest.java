package vector.UtilityBillingMS.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import vector.UtilityBillingMS.model.enums.MeterType;
import vector.UtilityBillingMS.model.enums.TariffType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class TariffRequest {
    @NotBlank(message = "Tariff name is required")
    private String name;

    @NotNull(message = "Utility type is required")
    private MeterType utilityType;

    @NotNull(message = "Tariff type is required")
    private TariffType tariffType;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    @NotNull(message = "VAT rate is required")
    @PositiveOrZero(message = "VAT rate cannot be negative")
    private BigDecimal vatRate;

    @NotNull(message = "Fixed service charge is required")
    @PositiveOrZero(message = "Fixed service charge cannot be negative")
    private BigDecimal fixedServiceCharge;

    @PositiveOrZero(message = "Penalty rate cannot be negative")
    private BigDecimal penaltyRate;

    @Positive(message = "Flat rate must be positive")
    private BigDecimal flatRate;

    @Valid
    private List<TariffTierRequest> tiers;
}
