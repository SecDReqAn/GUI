package general.entities;

import com.fasterxml.jackson.annotation.JsonView;
import io.securitycheck.AssumptionViews;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * An assumption that encompasses all properties (potentially) relevant to a security analysis.
 */
public class SecurityCheckAssumption implements Cloneable {
    /**
     * A {@link UUID} that uniquely identifies the {@link SecurityCheckAssumption} instance.
     */
    @JsonView(AssumptionViews.SecurityCheckAnalysisView.class)
    private @NotNull UUID id;
    /**
     * The type of the {@link SecurityCheckAssumption}, i.e., {@link AssumptionType#INTRODUCE_UNCERTAINTY}
     * or {@link AssumptionType#RESOLVE_UNCERTAINTY}.
     */
    @JsonView(AssumptionViews.SecurityCheckAnalysisView.class)
    protected @Nullable AssumptionType type;
    /**
     * A textual description of the {@link SecurityCheckAssumption}.
     */
    @JsonView(AssumptionViews.SecurityCheckAnalysisView.class)
    protected @Nullable String description;
    /**
     * A {@link Set} containing the model entities (i.e., {@link ModelEntity} instances) that are affected by
     * the {@link SecurityCheckAssumption}.
     */
    @JsonView(AssumptionViews.SecurityCheckAnalysisView.class)
    protected @NotNull Set<ModelEntity> affectedEntities;
    /**
     * A numerical value specifying the probability of violation for the {@link SecurityCheckAssumption}.
     */
    @JsonView(AssumptionViews.SecurityCheckAnalysisView.class)
    protected @Nullable Double probabilityOfViolation;
    /**
     * A textual description of the impact of the {@link SecurityCheckAssumption}.
     */
    @JsonView(AssumptionViews.SecurityCheckAnalysisView.class)
    protected @Nullable String impact;
    /**
     * A boolean value specifying whether the {@link SecurityCheckAssumption} has been analyzed.
     */
    @JsonView(AssumptionViews.SecurityCheckAnalysisView.class)
    protected boolean analyzed;

    /**
     * Default constructor that only initializes the <code>id</code> of the {@link SecurityCheckAssumption}
     * with a randomly generated {@link UUID}.
     */
    public SecurityCheckAssumption() {
        this(UUID.randomUUID());
    }

    /**
     * Constructor which sets the <code>id</code> of the {@link SecurityCheckAssumption} to the specified {@link UUID}
     * and minimally initializes the other fields.
     *
     * @param id The {@link UUID} that should be used as the {@link SecurityCheckAssumption}'s <code>id</code>.
     */
    public SecurityCheckAssumption(@NotNull UUID id) {
        this.id = id;
        this.affectedEntities = new HashSet<>();
        this.analyzed = false;
        // Implicitly set all other fields to null.
    }

    /**
     * Gets the <code>id</code> of the {@link SecurityCheckAssumption}.
     *
     * @return The {@link UUID} representing the <code>id</code>.
     */
    public @NotNull UUID getId() {
        return id;
    }

    /**
     * Gets the <code>type</code> of the {@link SecurityCheckAssumption}.
     *
     * @return The {@link AssumptionType} representing the <code>type</code> or <code>null</code> if the
     * <code>type</code> was not yet specified.
     */
    public @Nullable AssumptionType getType() {
        return type;
    }

    /**
     * Sets the <code>type</code> of the {@link SecurityCheckAssumption} to the specified value.
     *
     * @param type The {@link AssumptionType} that should be set.
     */
    public void setType(@Nullable AssumptionType type) {
        this.type = type;
    }

    /**
     * Gets the description of the {@link SecurityCheckAssumption}.
     *
     * @return The textual description as a {@link String} or <code>null</code> if the description
     * was not yet specified.
     */
    public @Nullable String getDescription() {
        return description;
    }

    /**
     * Sets the description of the {@link SecurityCheckAssumption} to the specified value.
     *
     * @param description The description that should be set.
     */
    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    /**
     * Gets a {@link Collection} of PCM model entities ({@link ModelEntity} instances) that are affected by
     * the {@link SecurityCheckAssumption}.
     *
     * @return The {@link Collection} of {@link ModelEntity} instances (<b>Note</b>: Can be empty).
     */
    public @NotNull Collection<ModelEntity> getAffectedEntities() {
        return this.affectedEntities;
    }

    /**
     * Gets the probability of violation for the {@link SecurityCheckAssumption}.
     *
     * @return The probability of violation as a {@link Double} or <code>null</code> if the probability was not
     * yet specified.
     */
    public @Nullable Double getProbabilityOfViolation() {
        return probabilityOfViolation;
    }

    /**
     * Sets the probability of violation for the {@link SecurityCheckAssumption}.
     *
     * @param probabilityOfViolation The probability that sould be set.
     */
    public void setProbabilityOfViolation(@Nullable Double probabilityOfViolation) {
        this.probabilityOfViolation = probabilityOfViolation;
    }

    /**
     * Gets the textual description of the {@link SecurityCheckAssumption}'s impact.
     *
     * @return The textual description of the {@link SecurityCheckAssumption}'s impact as a {@link String} or <code>null</code> if the impact
     * was not yet specified.
     */
    public @Nullable String getImpact() {
        return impact;
    }

    /**
     * Sets the impact of the {@link SecurityCheckAssumption}.
     *
     * @param impact The textual description of the impact that should be set.
     */
    public void setImpact(@Nullable String impact) {
        this.impact = impact;
    }

    /**
     * Gets whether the {@link SecurityCheckAssumption} was already analyzed.
     *
     * @return <code>true</code> if the {@link SecurityCheckAssumption} was already analyzed or <code>false</code>
     * otherwise.
     */
    public boolean isAnalyzed() {
        return analyzed;
    }

    /**
     * Sets whether the {@link SecurityCheckAssumption} was already analyzed.
     *
     * @param analyzed The analyzed-state that should be set.
     */
    public void setAnalyzed(boolean analyzed) {
        this.analyzed = analyzed;
    }

    /**
     * Clones this {@link AssumptionViews.SecurityCheckAnalysisView} instance.
     *
     * @return The created clone.
     */
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
