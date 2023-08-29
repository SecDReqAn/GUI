package general.entities;

import com.fasterxml.jackson.annotation.JsonView;
import io.AssumptionViews;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SecurityCheckAssumption implements Cloneable {
    @JsonView(AssumptionViews.SecurityCheckAnalysisView.class)
    private @NotNull UUID id;
    @JsonView(AssumptionViews.SecurityCheckAnalysisView.class)
    protected @Nullable AssumptionType type;
    @JsonView(AssumptionViews.SecurityCheckAnalysisView.class)
    protected @Nullable String description;
    @JsonView(AssumptionViews.SecurityCheckAnalysisView.class)
    protected @NotNull Set<ModelEntity> affectedEntities;
    @JsonView(AssumptionViews.SecurityCheckAnalysisView.class)
    protected @Nullable Double probabilityOfViolation;
    @JsonView(AssumptionViews.SecurityCheckAnalysisView.class)
    protected @Nullable String impact;
    @JsonView(AssumptionViews.SecurityCheckAnalysisView.class)
    protected boolean analyzed;

    public SecurityCheckAssumption() {
        this(UUID.randomUUID());
    }

    public SecurityCheckAssumption(@NotNull UUID id) {
        this.id = id;
        this.affectedEntities = new HashSet<>();
        this.analyzed = false;
        // Implicitly set all other fields to null.
    }

    public @NotNull UUID getId() {
        return id;
    }

    public @Nullable AssumptionType getType() {
        return type;
    }

    public void setType(@Nullable AssumptionType type) {
        this.type = type;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public Collection<ModelEntity> getAffectedEntities(){
        return this.affectedEntities;
    }

    public @Nullable Double getProbabilityOfViolation() {
        return probabilityOfViolation;
    }

    public void setProbabilityOfViolation(@Nullable Double probabilityOfViolation) {
        this.probabilityOfViolation = probabilityOfViolation;
    }

    public @Nullable String getImpact() {
        return impact;
    }

    public void setImpact(@Nullable String impact) {
        this.impact = impact;
    }

    public boolean isAnalyzed() {
        return analyzed;
    }

    public void setAnalyzed(boolean analyzed) {
        this.analyzed = analyzed;
    }

    @Override
    public SecurityCheckAssumption clone() {
        try {
            SecurityCheckAssumption clone = (SecurityCheckAssumption) super.clone();

            // UUID, String and primitive wrapper instances are immutable.
            clone.id = this.id;
            clone.type = this.type;
            clone.description = this.description;
            clone.affectedEntities = new HashSet<>(this.affectedEntities);
            clone.probabilityOfViolation = this.probabilityOfViolation;
            clone.impact = this.impact;
            clone.analyzed = this.analyzed;

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
