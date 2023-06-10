package general;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Assumption {
    public enum AssumptionType {
        INTRODUCE_UNCERTAINTY, RESOLVE_UNCERTAINTY;
    }

    private UUID id;
    private Set<UUID> dependencies;
    private String affectedComponent;
    private AssumptionType type;
    private String description;
    private Double probabilityOfViolation;
    private Double risk;
    private String impact;

    private Boolean analyzed;

    public Assumption() {
        this.id = UUID.randomUUID();
        this.dependencies = new HashSet<>();
        // Implicitly set all other fields to null.
    }

    public Assumption(UUID id) {
        this.id = id;
        this.dependencies = new HashSet<>();
        // Implicitly set all other fields to null.
    }

    public Assumption(AssumptionType type, String description, double probabilityOfViolation, double risk, String impact, boolean analyzed) {
        this.type = type;
        this.description = description;
        this.probabilityOfViolation = probabilityOfViolation;
        this.risk = risk;
        this.impact = impact;
        this.analyzed = analyzed;
    }

    public boolean isFullySpecified() {
        return this.affectedComponent != null &&
                this.type != null &&
                this.description != null &&
                this.probabilityOfViolation != null &&
                this.risk != null &&
                this.impact != null &&
                this.analyzed != null &&
                !this.impact.isEmpty() &&
                !this.description.isEmpty();
    }

    public void setAffectedComponent(String affectedComponent) {
        this.affectedComponent = affectedComponent;
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

    public String getAffectedComponent() {
        return this.affectedComponent;
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

    @Override
    public String toString() {
        return "Assumption{" +
                "id=" + id +
                ", dependencies=" + dependencies +
                ", affectedComponent='" + affectedComponent + '\'' +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", probabilityOfViolation=" + probabilityOfViolation +
                ", risk=" + risk +
                ", impact='" + impact + '\'' +
                ", analyzed=" + analyzed +
                '}';
    }
}
