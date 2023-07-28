package general;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class Configuration implements Cloneable {
    private String analysisPath;
    private String modelPath;
    private TreeSet<Assumption> assumptions;
    private String analysisResult;

    public Configuration() {
        this.assumptions = new TreeSet<>(new Assumption.AssumptionComparator());
    }

    public Configuration(String analysisPath, String modelPath, Set<Assumption> assumptions, String analysisResult) {
        this.analysisPath = analysisPath;
        this.modelPath = modelPath;
        this.analysisResult = analysisResult;
        this.assumptions = new TreeSet<>(new Assumption.AssumptionComparator());
        this.assumptions.addAll(assumptions);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return this.analysisPath == null &&
                this.modelPath == null &&
                this.assumptions == null &&
                this.analysisResult == null;
    }

    @JsonIgnore
    public boolean isMissingAnalysisParameters() {
        return this.analysisPath == null ||
                this.modelPath == null ||
                this.assumptions == null ||
                this.getAssumptions().isEmpty();
    }

    public String getAnalysisPath() {
        return analysisPath;
    }

    public void setAnalysisPath(String analysisPath) {
        this.analysisPath = analysisPath;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public Set<Assumption> getAssumptions() {
        return assumptions;
    }

    public void setAssumptions(Set<Assumption> assumptions) {
        if (assumptions == null) {
            this.assumptions = null;
        } else {
            this.assumptions = new TreeSet<>(new Assumption.AssumptionComparator());
            this.assumptions.addAll(assumptions);
        }
    }

    public String getAnalysisResult() {
        return analysisResult;
    }

    public void setAnalysisResult(String analysisResult) {
        this.analysisResult = analysisResult;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Configuration otherConfiguration) {
            boolean assumptionsEqual;
            if (this.assumptions == null || otherConfiguration.assumptions == null) {
                assumptionsEqual = this.assumptions == otherConfiguration.assumptions;
            } else {
                assumptionsEqual = this.assumptions.size() == otherConfiguration.assumptions.size();

                if (assumptionsEqual) {
                    var assumptionIteratorThis = this.assumptions.iterator();
                    var assumptionIteratorOther = otherConfiguration.assumptions.iterator();
                    while (assumptionIteratorThis.hasNext() && assumptionIteratorOther.hasNext())
                        if (!assumptionIteratorThis.next().semanticallyEqualTo(assumptionIteratorOther.next())) {
                            return false;
                        }
                }
            }

            return Objects.equals(this.analysisPath, otherConfiguration.analysisPath)
                    && Objects.equals(this.modelPath, otherConfiguration.modelPath)
                    && Objects.equals(this.analysisResult, otherConfiguration.analysisResult)
                    && assumptionsEqual;
        }

        return false;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "analysisPath='" + analysisPath + '\'' +
                ", modelPath='" + modelPath + '\'' +
                ", assumptions=" + assumptions +
                ", analysisResult='" + analysisResult + '\'' +
                '}';
    }

    @Override
    public Configuration clone() {
        try {
            Configuration clone = (Configuration) super.clone();
            clone.analysisPath = this.analysisPath;
            clone.modelPath = this.modelPath;
            clone.analysisResult = this.analysisResult;

            clone.assumptions = new TreeSet<>(new Assumption.AssumptionComparator());
            this.assumptions.forEach(assumption -> clone.assumptions.add(assumption.clone()));

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
