package general.entities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

// Decided against base class of assumption due to concerns with serializing assumptions as their base class.
public record SecurityCheckAssumption(@NotNull UUID id,
                                      @NotNull Assumption.AssumptionType type,
                                      @NotNull String description,
                                      @NotNull Set<ModelEntity> affectedEntities,
                                      @Nullable Double probabilityOfViolation,
                                      @Nullable String impact,
                                      boolean analyzed) {

    public static SecurityCheckAssumption fromAssumption(@NotNull Assumption assumption) {
        return new SecurityCheckAssumption(assumption.getId(),
                assumption.getType(),
                assumption.getDescription(),
                assumption.getAffectedEntities(),
                assumption.getProbabilityOfViolation(),
                assumption.getImpact(),
                assumption.getManuallyAnalyzed());
    }
}
