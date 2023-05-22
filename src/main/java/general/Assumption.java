package general;

public class Assumption {
    public enum AssumptionType {
        INTRODUCE_UNCERTAINTY, RESOLVE_UNCERTAINTY;
    }

    private AssumptionType type;
    private String description;
    private double probabilityOfViolation;
    private double risk;
    private String impact;
    private boolean analyzed;

    public Assumption(){

    }

    public Assumption(AssumptionType type, String description, double probabilityOfViolation, double risk, String impact, boolean analyzed) {
        this.type = type;
        this.description = description;
        this.probabilityOfViolation = probabilityOfViolation;
        this.risk = risk;
        this.impact = impact;
        this.analyzed = analyzed;
    }

    public void setType(AssumptionType type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProbabilityOfViolation(double probabilityOfViolation) {
        this.probabilityOfViolation = probabilityOfViolation;
    }

    public void setRisk(double risk) {
        this.risk = risk;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public void setAnalyzed(boolean analyzed) {
        this.analyzed = analyzed;
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
