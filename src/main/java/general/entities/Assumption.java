package general.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import io.AssumptionViews;
import org.jetbrains.annotations.NotNull;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getManuallyAnalyzed() {
        return this.manuallyAnalyzed;
    }

    public void setManuallyAnalyzed(boolean manuallyAnalyzed) {
        this.manuallyAnalyzed = manuallyAnalyzed;
    }

    public void setRisk(Double risk) {
        this.risk = risk;
    }

    public Collection<UUID> getDependencies() {
        return this.dependencies;
    }

    public Double getRisk() {
        return this.risk;
    }

    @JsonIgnore
    public boolean isSufficientlySpecified() {
        return this.type != null &&
                this.name != null &&
                this.description != null &&
                !this.name.isEmpty() &&
                !this.description.isEmpty();
    }

    public boolean semanticallyEqualTo(Assumption otherAssumption) {
        return Objects.equals(this.getId(), otherAssumption.getId()) &&
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

    public void updateWith(SecurityCheckAssumption securityCheckAssumption) {
        assert securityCheckAssumption.getId().equals(this.getId());

        this.type = securityCheckAssumption.type;
        this.description = securityCheckAssumption.description;
        this.affectedEntities = securityCheckAssumption.affectedEntities;
        this.probabilityOfViolation = securityCheckAssumption.probabilityOfViolation;
        this.impact = securityCheckAssumption.impact;
        this.analyzed = securityCheckAssumption.analyzed;
    }

    @Override
    public Assumption clone() {
        Assumption clone = (Assumption) super.clone();

        // UUID, String and primitive wrapper instances are immutable.
        clone.name = this.name;
        clone.manuallyAnalyzed = this.manuallyAnalyzed;
        clone.dependencies = new HashSet<>(this.dependencies);
        clone.risk = this.risk;

        return clone;
    }
}
