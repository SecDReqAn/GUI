package general.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class representing the core application state of Assumption Analyzer. It contains all data that, from a
 * user-viewpoint, is worth persisting.
 */
public class Configuration implements Cloneable {
    /**
     * The URI {@link String} to the security analysis.
     */
    @Nullable
    private String analysisPath;
    /**
     * The absolute path to the PCM.
     */
    @Nullable
    private String modelPath;
    /**
     * The {@link Set} of specified {@link GraphAssumption}s.
     */
    @NotNull
    private Set<GraphAssumption> assumptions;
    /**
     * The {@link Set} of previous {@link AnalysisResult}s.
     */
    @NotNull
    private Set<AnalysisResult> analysisResults;

    /**
     * Default constructor.
     */
    public Configuration() {
        this.assumptions = new HashSet<>();
        this.analysisResults = new HashSet<>();
    }

    /**
     * Constructor that creates a new instance and initializes it with the specified properties.
     *
     * @param analysisPath    The URI {@link String} to the security analysis that should be set.
     * @param modelPath       The absolute path to the PCM that should be set.
     * @param assumptions     The {@link Set} of specified {@link GraphAssumption}s that should be set.
     * @param analysisResults The {@link Set} of previous {@link AnalysisResult}s that should be set.
     */
    public Configuration(@Nullable String analysisPath, @Nullable String modelPath,
                         @NotNull Set<GraphAssumption> assumptions, @NotNull Set<AnalysisResult> analysisResults) {
        this.analysisPath = analysisPath;
        this.modelPath = modelPath;
        this.analysisResults = new HashSet<>();
        this.analysisResults.addAll(analysisResults);
        this.assumptions = new HashSet<>();
        this.assumptions.addAll(assumptions);
    }

    /**
     * Checks whether the {@link Configuration} is missing data required for a security analysis execution.
     *
     * @return <code>true</code> if the {@link Configuration} is missing data or <code>false</code> otherwise.
     */
    @JsonIgnore
    public boolean isMissingAnalysisParameters() {
        return this.analysisPath == null ||
                this.modelPath == null ||
                this.getAssumptions().isEmpty();
    }

    /**
     * Gets the URI {@link String} of the security analysis specified as part of the {@link Configuration}.
     *
     * @return The URI {@link String} of the security analysis microservice or <code>null</code> if no path has been
     * set by the user.
     */
    public @Nullable String getAnalysisPath() {
        return analysisPath;
    }

    /**
     * Sets the URI {@link String} of the security analysis.
     *
     * @param analysisPath The URI {@link String} that should be set.
     */
    public void setAnalysisPath(@NotNull String analysisPath) {
        this.analysisPath = analysisPath;
    }

    /**
     * Gets the absolute path to the PCM.
     *
     * @return The absolute path (as a {@link String}).
     */
    public @Nullable String getModelPath() {
        return modelPath;
    }

    /**
     * Sets the absolute path to the PCM.
     *
     * @param modelPath The absolute path (as a {@link String}) that should be set.
     */
    public void setModelPath(@NotNull String modelPath) {
        this.modelPath = modelPath;
    }

    /**
     * Gets the {@link Set} of specified {@link GraphAssumption}s.
     *
     * @return The {@link Set} of {@link GraphAssumption}s
     */
    public @NotNull Set<GraphAssumption> getAssumptions() {
        return assumptions;
    }

    /**
     * Gets the {@link Set} of previous {@link AnalysisResult}s.
     *
     * @return The {@link Set} of {@link AnalysisResult}s
     */
    public @NotNull Set<AnalysisResult> getAnalysisResults() {
        return analysisResults;
    }

    /**
     * Checks whether this {@link Configuration} is semantically equal to <code>otherConfiguration</code>, meaning
     * that they both contain the same semantic values.
     *
     * <p><b>Note</b>: Even two different {@link Configuration} instances can be semantically equal if their fields
     * are all semantically equal (for instance, two different {@link String} instances would be regarded as
     * semantically equal if they encapsulate the same character sequence).</p>
     *
     * @param otherConfiguration The {@link Configuration} that should be semantically compared to this
     *                           {@link Configuration}.
     * @return <code>true</code> if both {@link Configuration}s are semantically equal and <code>false</code> otherwise.
     */
    public boolean semanticallyEqualTo(Configuration otherConfiguration) {
        // Check whether the Assumption instances are semantically identical.
        if (this.assumptions.size() != otherConfiguration.assumptions.size()) {
            return false;
        }

        ArrayList<GraphAssumption> assumptionsThis = new ArrayList<>(this.assumptions);
        ArrayList<GraphAssumption> assumptionsOther = new ArrayList<>(otherConfiguration.assumptions);
        var assumptionComparator = new GraphAssumption.AssumptionComparator();
        assumptionsThis.sort(assumptionComparator);
        assumptionsOther.sort(assumptionComparator);
        for (int i = 0; i < assumptionsThis.size(); i++) {
            if (!assumptionsThis.get(i).semanticallyEqualTo(assumptionsOther.get(i))) {
                return false;
            }
        }


        // Check whether AnalysisResult instances are semantically identical (titles are assumed to be unique
        // identifiers and, therefore, suffice for the comparison).
        if (this.analysisResults.size() != otherConfiguration.analysisResults.size()) {
            return false;
        }

        Set<String> titlesThis = this.analysisResults.stream().map(AnalysisResult::getTitle).collect(
                Collectors.toSet());
        // Enforce HashSet on other set for O(1) lookup.
        HashSet<String> titlesOther = otherConfiguration.analysisResults.stream()
                .map(AnalysisResult::getTitle)
                .collect(Collectors.toCollection(HashSet::new));
        boolean analysisResultsEqual = titlesThis.stream().filter(
                title -> !titlesOther.contains(title)).findFirst().isEmpty();


        return analysisResultsEqual
                && Objects.equals(this.analysisPath, otherConfiguration.analysisPath)
                && Objects.equals(this.modelPath, otherConfiguration.modelPath);
    }

    /**
     * Creates a clone of this {@link Configuration}.
     *
     * @return The created clone.
     */
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
