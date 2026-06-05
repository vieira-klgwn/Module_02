package vector.UtilityBillingMS.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tariff_tiers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffTier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id", nullable = false)
    @JsonIgnore
    private Tariff tariff;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal minConsumption;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxConsumption;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal ratePerUnit;
}
