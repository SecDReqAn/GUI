package general.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
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
    private String name;
    private AssumptionType type;
    private Set<ModelEntity> affectedEntities;
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
        this.affectedEntities = new HashSet<>();
        // Implicitly set all other fields to null.
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAffectedEntities(Set<ModelEntity> affectedEntities) {
        this.affectedEntities = affectedEntities;
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

    public Set<ModelEntity> getAffectedEntities() {
        return this.affectedEntities;
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

    public Double getProbabilityOfViolation() {
        return this.probabilityOfViolation;
    }

    public Double getRisk() {
        return this.risk;
    }

    public String getImpact() {
        return this.impact;
    }

    public Boolean isAnalyzed() {
        return this.analyzed;
    }

    @JsonIgnore
    public boolean isSufficientlySpecified() {
        return this.type != null &&
                this.name != null &&
                this.description != null &&
                !this.name.isEmpty() &&
                !this.description.isEmpty();
    }

    public boolean semanticallyEqualTo(Assumption otherAssumption){
        return Objects.equals(this.id, otherAssumption.id) &&
                Objects.equals(this.name, otherAssumption.name) &&
                Objects.equals(this.type, otherAssumption.type) &&
                Objects.equals(this.affectedEntities, otherAssumption.affectedEntities) &&
                Objects.equals(this.dependencies, otherAssumption.dependencies) &&
                Objects.equals(this.description, otherAssumption.description)&&
                Objects.equals(this.probabilityOfViolation, otherAssumption.probabilityOfViolation) &&
                Objects.equals(this.risk, otherAssumption.risk) &&
                Objects.equals(this.impact, otherAssumption.impact) &&
                Objects.equals(this.analyzed, otherAssumption.analyzed);
    }

    @Override
    public String toString() {
        return "id=" + id +
                ", description='" + description + '\'' +
                ", affectedComponent='" + affectedEntities + '\'' +
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
            clone.affectedEntities = new HashSet<>(this.affectedEntities);
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
