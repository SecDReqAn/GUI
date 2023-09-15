package general.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import io.securitycheck.AssumptionViews;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

// TODO Find better name for class.

/**
 * An extended assumption that extends a {@link SecurityCheckAssumption} by some properties tailored towards a
 * more high-level analysis.
 *
 * <p><b>Note</b>: @{@link Nullable} and @{@link NotNull} annotations for the fields are intentionally omitted as, on
 * initialization of the object, nearly all fields are <code>null</code>. However,
 * {@link controllers.AssumptionSpecificationScreenController} ensures that the the mandatory fields are always set
 * during the specification.</p>
 */
public class Assumption extends SecurityCheckAssumption implements Cloneable {
    /**
     * Custom {@link Comparator} that compares {@link Assumption}s based on their unique IDs.
     */
    public static class AssumptionComparator implements Comparator<Assumption> {
        @Override
        public int compare(Assumption a1, Assumption a2) {
            return a1.getId().compareTo(a2.getId());
        }
    }

    /**
     * The user-specified name of the {@link Assumption}.
     */
    @JsonView(AssumptionViews.AssumptionGraphAnalysisView.class)
    private String name;
    /**
     * A flag indicating whether the user marked the {@link Assumption} as manually analyzed.
     */
    @JsonView(AssumptionViews.AssumptionGraphAnalysisView.class)
    private boolean manuallyAnalyzed;
    /**
     * A {@link Set} containing the unique <code>id</code>s of all {@link Assumption}s on which this
     * {@link Assumption} depends.
     */
    @JsonView(AssumptionViews.AssumptionGraphAnalysisView.class)
    private Set<UUID> dependencies;
    /**
     * The risk associated with the {@link Assumption}.
     */
    @JsonView(AssumptionViews.AssumptionGraphAnalysisView.class)
    private Double risk; // -

    /**
     * Default constructor that only initializes the <code>id</code> of the {@link Assumption}
     * with a randomly generated {@link UUID}.
     */
    public Assumption() {
        super();
        this.dependencies = new HashSet<>();
    }

    /**
     * Constructor which sets the <code>id</code> of the {@link Assumption} to the specified {@link UUID}
     * and minimally initializes the other fields.
     *
     * @param id The {@link UUID} that should be used as the {@link Assumption}'s <code>id</code>.
     */
    public Assumption(@NotNull UUID id) {
        super(id);
        this.dependencies = new HashSet<>();
        this.manuallyAnalyzed = false;
        // Implicitly set all other fields to null.
    }

    /**
     * Gets the name of the {@link Assumption}.
     *
     * @return The name of the assumption.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the {@link Assumption}.
     *
     * @param name The name that should be set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the <code>manuallyAnalyzed</code> property of the {@link Assumption}.
     *
     * @return The <code>manuallyAnalyzed</code> property.
     */
    public boolean getManuallyAnalyzed() {
        return this.manuallyAnalyzed;
    }

    /**
     * Sets the <code>manuallyAnalyzed</code> property of the {@link Assumption}.
     *
     * @param manuallyAnalyzed The state that should be set.
     */
    public void setManuallyAnalyzed(boolean manuallyAnalyzed) {
        this.manuallyAnalyzed = manuallyAnalyzed;
    }

    /**
     * Gets the numerical risk associated with the {@link Assumption}.
     *
     * @return The risk that associated with the assumption.
     */
    public Double getRisk() {
        return this.risk;
    }

    /**
     * Sets the numerical risk associated with the {@link Assumption}.
     *
     * @param risk The risk that should be set.
     */
    public void setRisk(double risk) {
        this.risk = risk;
    }

    /**
     * Gets the {@link Collection} containing the {@link UUID}s of the {@link Assumption}s on which this
     * {@link Assumption} is depending.
     *
     * @return The {@link Collection} containing the {@link UUID}s.
     */
    public Collection<UUID> getDependencies() {
        return this.dependencies;
    }

    /**
     * Checks whether all mandatory fields of the {@link Assumption} (i.e., <code>type</code>, <code>name</code>,
     * and <code>description</code> have been sufficiently specified).
     *
     * @return <code>true</code> if all the mandatory fields are sufficiently specified and <code>false</code>
     * otherwise.
     */
    @JsonIgnore
    public boolean isSufficientlySpecified() {
        return this.type != null &&
                this.name != null &&
                this.description != null &&
                !this.name.isEmpty() &&
                !this.description.isEmpty();
    }

    /**
     * Checks whether this {@link Assumption} is semantically equal to <code>otherAssumption</code>,
     * meaning that they both contain the same semantic values.
     *
     * <p><b>Note</b>: Even two different {@link Assumption} instances can be semantically equal if they contain the
     * same values (w.r.t. <code>equals</code> of the individual fields).</p>
     *
     * @param otherAssumption The {@link Assumption} that should be semantically compared to this {@link Assumption}.
     * @return <code>true</code> if both {@link Assumption}s are semantically equal and <code>false</code> otherwise.
     */
    public boolean semanticallyEqualTo(@Nullable Assumption otherAssumption) {
        return otherAssumption != null &&
                Objects.equals(this.getId(), otherAssumption.getId()) &&
                Objects.equals(this.name, otherAssumption.name) &&
                Objects.equals(this.type, otherAssumption.type) &&
                Objects.equals(this.affectedEntities, otherAssumption.affectedEntities) &&
                Objects.equals(this.dependencies, otherAssumption.dependencies) &&
                Objects.equals(this.description, otherAssumption.description) &&
                Objects.equals(this.probabilityOfViolation, otherAssumption.probabilityOfViolation) &&
                Objects.equals(this.risk, otherAssumption.risk) &&
                Objects.equals(this.impact, otherAssumption.impact) &&
                Objects.equals(this.analyzed, otherAssumption.analyzed) &&
                this.manuallyAnalyzed == otherAssumption.manuallyAnalyzed;
    }

    /**
     * Updates the fields of this {@link Assumption} based on the information contained in the specified
     * {@link SecurityCheckAssumption}.
     *
     * @param securityCheckAssumption The {@link SecurityCheckAssumption} instance with whose information this
     *                                {@link Assumption} should be updated.
     */
    public void updateWith(@NotNull SecurityCheckAssumption securityCheckAssumption) {
        assert securityCheckAssumption.getId().equals(this.getId());

        this.type = securityCheckAssumption.type;
        this.description = securityCheckAssumption.description;
        this.affectedEntities = securityCheckAssumption.affectedEntities;
        this.probabilityOfViolation = securityCheckAssumption.probabilityOfViolation;
        this.impact = securityCheckAssumption.impact;
        this.analyzed = securityCheckAssumption.analyzed;
    }

    /**
     * Creates a clone of this {@link Assumption}.
     *
     * @return The created clone.
     */
    @Override
    public @NotNull Assumption clone() {
        Assumption clone = (Assumption) super.clone();

        // UUID, String and primitive wrapper instances are immutable.
        clone.name = this.name;
        clone.manuallyAnalyzed = this.manuallyAnalyzed;
        clone.dependencies = new HashSet<>(this.dependencies);
        clone.risk = this.risk;

        return clone;
    }
}
