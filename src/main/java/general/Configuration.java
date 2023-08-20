package general;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Configuration implements Cloneable {
    public static class AnalysisResult implements Cloneable {
        private String title;
        private String result;

        public AnalysisResult(@NotNull String title, @NotNull String result){
            this.title = title;
            this.result = result;
        }

        public String getTitle() {
            return title;
        }

        public String getResult() {
            return result;
        }

        @Override
        public AnalysisResult clone() {
            try {
                AnalysisResult clone = (AnalysisResult) super.clone();

                clone.title = this.title;
                clone.result = this.result;

                return clone;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }

    private String analysisPath;
    private String modelPath;
    // TODO: Fix deserialization bug where the custom comparator cannot be found (a subsequent cast to Comparable obviously fails).
    private SortedSet<Assumption> assumptions; // Require sorted property for efficient comparison of configurations.
    private Set<AnalysisResult> analysisResults;

    public Configuration() {
        this.assumptions = new TreeSet<>(new Assumption.AssumptionComparator());
        this.analysisResults = new HashSet<>();
    }

    public Configuration(String analysisPath, String modelPath, Set<Assumption> assumptions, Set<AnalysisResult> analysisResults) {
        this.analysisPath = analysisPath;
        this.modelPath = modelPath;
        this.analysisResults = new HashSet<>();
        this.analysisResults.addAll(analysisResults);
        this.assumptions = new TreeSet<>(new Assumption.AssumptionComparator());
        this.assumptions.addAll(assumptions);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return this.analysisPath == null &&
                this.modelPath == null &&
                this.assumptions.isEmpty() &&
                this.analysisResults.isEmpty();
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

    public Set<AnalysisResult> getAnalysisResults() {
        return analysisResults;
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
                    && Objects.equals(this.analysisResults, otherConfiguration.analysisResults)
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
                ", analysisResults=" + analysisResults +
                '}';
    }

    @Override
    public Configuration clone() {
        try {
            Configuration clone = (Configuration) super.clone();
            clone.analysisPath = this.analysisPath;
            clone.modelPath = this.modelPath;

            clone.analysisResults = new HashSet<>();
            this.analysisResults.forEach(analysisResult -> clone.analysisResults.add(analysisResult.clone()));

            clone.assumptions = new TreeSet<>(new Assumption.AssumptionComparator());
            this.assumptions.forEach(assumption -> clone.assumptions.add(assumption.clone()));

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
