package general;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Configuration implements Cloneable {
    public static class AnalysisResult implements Cloneable {
        /**
         * The title of the {@link AnalysisResult} (by default the current date and time in the format dd.mm.yy hh:mm:ss).
         *
         * <p>
         * Note: The title attribute uniquely identifies an {@link AnalysisResult} within a {@link Configuration} (i.e., is is strictly forbidden for two {@link AnalysisResult}s to share the same title).
         * </p>
         */
        private @NotNull String title;
        private @NotNull String result;

        /**
         * Default constructor as <b>required</b> for Jackson deserialization.
         */
        @SuppressWarnings("unused")
        private AnalysisResult() {
            title = result = "";
        }

        public AnalysisResult(@NotNull String title, @NotNull String result) {
            this.title = title;
            this.result = result;
        }

        public @NotNull String getTitle() {
            return title;
        }

        public @NotNull String getResult() {
            return result;
        }

        @Override
        public @NotNull AnalysisResult clone() {
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

    @Nullable
    private String analysisPath;
    @Nullable
    private String modelPath;
    @NotNull
    private Set<Assumption> assumptions;
    @NotNull
    private Set<AnalysisResult> analysisResults;

    public Configuration() {
        this.assumptions = new HashSet<>();
        this.analysisResults = new HashSet<>();
    }

    public Configuration(@Nullable String analysisPath, @Nullable String modelPath, @NotNull Set<Assumption> assumptions, @NotNull Set<AnalysisResult> analysisResults) {
        this.analysisPath = analysisPath;
        this.modelPath = modelPath;
        this.analysisResults = new HashSet<>();
        this.analysisResults.addAll(analysisResults);
        this.assumptions = new HashSet<>();
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
                this.getAssumptions().isEmpty();
    }

    public @Nullable String getAnalysisPath() {
        return analysisPath;
    }

    public void setAnalysisPath(@Nullable String analysisPath) {
        this.analysisPath = analysisPath;
    }

    public @Nullable String getModelPath() {
        return modelPath;
    }

    public void setModelPath(@Nullable String modelPath) {
        this.modelPath = modelPath;
    }

    public @NotNull Set<Assumption> getAssumptions() {
        return assumptions;
    }

    public @NotNull Set<AnalysisResult> getAnalysisResults() {
        return analysisResults;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Configuration otherConfiguration) {
            // Check whether the Assumption instances are semantically identical.
            if (this.assumptions.size() != otherConfiguration.assumptions.size()) {
                return false;
            }

            ArrayList<Assumption> assumptionsThis = new ArrayList<>(this.assumptions);
            ArrayList<Assumption> assumptionsOther = new ArrayList<>(otherConfiguration.assumptions);
            var assumptionComparator = new Assumption.AssumptionComparator();
            assumptionsThis.sort(assumptionComparator);
            assumptionsOther.sort(assumptionComparator);
            for (int i = 0; i < assumptionsThis.size(); i++) {
                if (!assumptionsThis.get(i).semanticallyEqualTo(assumptionsOther.get(i))) {
                    return false;
                }
            }


            // Check whether AnalysisResult instances are semantically identical (titles are assumed to be unique identifiers).
            if (this.analysisResults.size() != otherConfiguration.analysisResults.size()) {
                return false;
            }

            Set<String> titlesThis = this.analysisResults.stream().map(AnalysisResult::getTitle).collect(Collectors.toSet());
            // Enforce HashSet on other set for O(1) lookup.
            HashSet<String> titlesOther = otherConfiguration.analysisResults.stream().map(AnalysisResult::getTitle).collect(Collectors.toCollection(HashSet::new));
            boolean analysisResultsEqual = titlesThis.stream().filter(title -> !titlesOther.contains(title)).findFirst().isEmpty();


            return analysisResultsEqual
                    && Objects.equals(this.analysisPath, otherConfiguration.analysisPath)
                    && Objects.equals(this.modelPath, otherConfiguration.modelPath);
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

            clone.assumptions = new HashSet<>();
            this.assumptions.forEach(assumption -> clone.assumptions.add(assumption.clone()));

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
