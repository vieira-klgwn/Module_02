package vector.UtilityBillingMS.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MeterReadingRequest {
    @NotNull(message = "Meter ID is required")
    private Long meterId;

    @NotNull(message = "Previous reading is required")
    @Positive(message = "Previous reading must be positive")
    private BigDecimal previousReading;

    @NotNull(message = "Current reading is required")
    @Positive(message = "Current reading must be positive")
    private BigDecimal currentReading;

    @NotNull(message = "Reading date is required")
    private LocalDate readingDate;
}
