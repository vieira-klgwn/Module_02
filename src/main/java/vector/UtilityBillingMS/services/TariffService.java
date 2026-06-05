package vector.UtilityBillingMS.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vector.UtilityBillingMS.exceptions.BusinessException;
import vector.UtilityBillingMS.model.Tariff;
import vector.UtilityBillingMS.model.TariffTier;
import vector.UtilityBillingMS.model.dto.TariffRequest;
import vector.UtilityBillingMS.model.dto.TariffTierRequest;
import vector.UtilityBillingMS.model.enums.TariffType;
import vector.UtilityBillingMS.repositories.TariffRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TariffService {

    private final TariffRepository tariffRepository;

    @Transactional
    public Tariff create(TariffRequest request) {
        validateTariffRequest(request);

        LocalDate today = LocalDate.now();
        var existingLatest = tariffRepository.findTopByUtilityTypeOrderByVersionDesc(request.getUtilityType());
        int nextVersion = existingLatest.map(t -> t.getVersion() + 1).orElse(1);

        if (existingLatest.isPresent() && !request.getEffectiveFrom().isAfter(today)) {
            throw new BusinessException("New tariff versions must have an effective date in the future");
        }

        tariffRepository.findApplicableTariffs(request.getUtilityType(), request.getEffectiveFrom())
                .stream()
                .filter(t -> t.getEffectiveTo() == null)
                .findFirst()
                .ifPresent(current -> {
                    current.setEffectiveTo(request.getEffectiveFrom().minusDays(1));
                    tariffRepository.save(current);
                });

        Tariff tariff = Tariff.builder()
                .name(request.getName())
                .utilityType(request.getUtilityType())
                .tariffType(request.getTariffType())
                .version(nextVersion)
                .effectiveFrom(request.getEffectiveFrom())
                .vatRate(request.getVatRate())
                .fixedServiceCharge(request.getFixedServiceCharge())
                .penaltyRate(request.getPenaltyRate() != null ? request.getPenaltyRate() : BigDecimal.ZERO)
                .flatRate(request.getFlatRate())
                .build();

        if (request.getTariffType() == TariffType.TIERED && request.getTiers() != null) {
            for (TariffTierRequest tierReq : request.getTiers()) {
                TariffTier tier = TariffTier.builder()
                        .tariff(tariff)
                        .minConsumption(tierReq.getMinConsumption())
                        .maxConsumption(tierReq.getMaxConsumption())
                        .ratePerUnit(tierReq.getRatePerUnit())
                        .build();
                tariff.getTiers().add(tier);
            }
        }

        return tariffRepository.save(tariff);
    }

    private void validateTariffRequest(TariffRequest request) {
        if (request.getVatRate().compareTo(BigDecimal.ZERO) < 0
                || request.getFixedServiceCharge().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("VAT and fixed charges cannot be negative");
        }
        if (request.getTariffType() == TariffType.FLAT) {
            if (request.getFlatRate() == null || request.getFlatRate().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Flat rate is required and must be positive for flat tariffs");
            }
        } else if (request.getTiers() == null || request.getTiers().isEmpty()) {
            throw new BusinessException("At least one tier is required for tiered tariffs");
        }
    }

    public List<Tariff> findAll() {
        return tariffRepository.findAll();
    }

    public Tariff findById(Long id) {
        return tariffRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Tariff not found with id: " + id));
    }

    public Tariff findApplicableTariff(vector.UtilityBillingMS.model.enums.MeterType type, LocalDate date) {
        return tariffRepository.findApplicableTariff(type, date)
                .orElseThrow(() -> new BusinessException("No applicable tariff found for " + type + " on " + date));
    }

    public BigDecimal calculateConsumptionCost(Tariff tariff, BigDecimal consumption) {
        if (tariff.getTariffType() == TariffType.FLAT) {
            return consumption.multiply(tariff.getFlatRate());
        }
        BigDecimal remaining = consumption;
        BigDecimal cost = BigDecimal.ZERO;
        for (TariffTier tier : tariff.getTiers()) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal tierMax = tier.getMaxConsumption() != null
                    ? tier.getMaxConsumption().subtract(tier.getMinConsumption())
                    : remaining;
            BigDecimal unitsInTier = remaining.min(tierMax);
            if (unitsInTier.compareTo(BigDecimal.ZERO) > 0) {
                cost = cost.add(unitsInTier.multiply(tier.getRatePerUnit()));
                remaining = remaining.subtract(unitsInTier);
            }
        }
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            TariffTier lastTier = tariff.getTiers().getLast();
            cost = cost.add(remaining.multiply(lastTier.getRatePerUnit()));
        }
        return cost;
    }
}
