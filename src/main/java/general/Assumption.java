package general;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Assumption implements Cloneable{
    public static class AssumptionComparator implements Comparator<Assumption>{
        @Override
        public int compare(Assumption a1, Assumption a2) {
            return a1.id.compareTo(a2.id);
        }
    }
    public enum AssumptionType {
        INTRODUCE_UNCERTAINTY, RESOLVE_UNCERTAINTY;

        @Override
        public String toString() {
            if(this == INTRODUCE_UNCERTAINTY){
                return "Introduce Uncertainty";
            } else {
                return "Resolve Uncertainty";
            }
        }
    }

    private UUID id;
    private AssumptionType type;
    private String affectedEntity; // TODO An assumption is able to affect multiple model-entities.
    private Set<UUID> dependencies;
    private String description;
    private Double probabilityOfViolation;
    private Double risk;
    private String impact;
    private Boolean analyzed;

    public Assumption() {
        this(UUID.randomUUID());
    }

    public Assumption(UUID id) {
        this.id = id;
        this.dependencies = new HashSet<>();
        // Implicitly set all other fields to null.
    }

    public Assumption(AssumptionType type, String description, double probabilityOfViolation, double risk, String impact, boolean analyzed) {
        this();
        this.type = type;
        this.description = description;
        this.probabilityOfViolation = probabilityOfViolation;
        this.risk = risk;
        this.impact = impact;
        this.analyzed = analyzed;
    }

    public void setAffectedEntity(String affectedEntity) {
        this.affectedEntity = affectedEntity;
    }

    public void setDependencies(Set<UUID> dependencies) {
        this.dependencies = dependencies;
    }

    public void setType(AssumptionType type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProbabilityOfViolation(Double probabilityOfViolation) {
        this.probabilityOfViolation = probabilityOfViolation;
    }

    public void setRisk(Double risk) {
        this.risk = risk;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public void setAnalyzed(Boolean analyzed) {
        this.analyzed = analyzed;
    }

    public UUID getId() {
        return this.id;
    }

    public String getAffectedEntity() {
        return this.affectedEntity;
    }

    public Set<UUID> getDependencies() {
        return this.dependencies;
    }

    public AssumptionType getType() {
        return this.type;
    }

    public String getDescription() {
        return this.description;
    }

    public double getProbabilityOfViolation() {
        return this.probabilityOfViolation;
    }

    public double getRisk() {
        return this.risk;
    }

    public String getImpact() {
        return this.impact;
    }

    public boolean isAnalyzed() {
        return this.analyzed;
    }

    @JsonIgnore
    public boolean isFullySpecified() {
        return this.affectedEntity != null &&
                this.type != null &&
                this.description != null &&
                this.probabilityOfViolation != null &&
                this.risk != null &&
                this.impact != null &&
                this.analyzed != null &&
                !this.impact.isEmpty() &&
                !this.description.isEmpty();
    }

    @Override
    public String toString() {
        return "id=" + id +
                ", description='" + description + '\'' +
                ", affectedComponent='" + affectedEntity + '\'' +
                ", type=" + type +
                ", dependencies=" + dependencies +
                ", probabilityOfViolation=" + probabilityOfViolation +
                ", risk=" + risk +
                ", impact='" + impact + '\'' +
                ", analyzed=" + analyzed;
    }

    @Override
    public Assumption clone() {
        try {
            Assumption clone = (Assumption) super.clone();

            // UUID, String and primitive wrapper instances are immutable.
            clone.id = this.id;
            clone.dependencies = new HashSet<>(this.dependencies);
            clone.affectedEntity = this.affectedEntity;
            clone.type = this.type;
            clone.description = this.description;
            clone.probabilityOfViolation = this.probabilityOfViolation;
            clone.risk = this.risk;
            clone.impact = this.impact;
            clone.analyzed = this.analyzed;

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
