package general;

public class Assumption {
    public enum AssumptionType {
        INTRODUCE_UNCERTAINTY, RESOLVE_UNCERTAINTY;
    }

    private AssumptionType type;
    private String description;
    private Double probabilityOfViolation;
    private Double risk;
    private String impact;
    private Boolean analyzed;

    public Assumption(){
        // Implicitly set all fields to null.
    }

    public Assumption(AssumptionType type, String description, double probabilityOfViolation, double risk, String impact, boolean analyzed) {
        this.type = type;
        this.description = description;
        this.probabilityOfViolation = probabilityOfViolation;
        this.risk = risk;
        this.impact = impact;
        this.analyzed = analyzed;
    }

    public boolean isFullySpecified(){
        return this.type != null &&
                this.description != null &&
                this.probabilityOfViolation != null &&
                this.risk != null &&
                this.impact != null &&
                this.analyzed != null &&
                !this.impact.isEmpty() &&
                !this.description.isEmpty();
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

    public AssumptionType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public double getProbabilityOfViolation() {
        return probabilityOfViolation;
    }

    public double getRisk() {
        return risk;
    }

    public String getImpact() {
        return impact;
    }

    public boolean isAnalyzed() {
        return analyzed;
    }

    @Override
    public String toString() {
        return "Assumption{" +
                "type=" + type +
                ", description='" + description + '\'' +
                ", probabilityOfViolation=" + probabilityOfViolation +
                ", risk=" + risk +
                ", impact='" + impact + '\'' +
                ", analyzed=" + analyzed +
                '}';
    }
}
