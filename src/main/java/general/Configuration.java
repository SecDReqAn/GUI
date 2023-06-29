package general;

import java.util.Set;

// TODO Add field for the result of an analysis.

public class Configuration {
    private String analysisPath;
    private String modelPath;
    private Set<Assumption> assumptions;

    public Configuration() {
    }

    public Configuration(String analysisPath, String modelPath, Set<Assumption> assumptions) {
        this.analysisPath = analysisPath;
        this.modelPath = modelPath;
        this.assumptions = assumptions;
    }

    public void setAnalysisPath(String analysisPath) {
        this.analysisPath = analysisPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public void setAssumptions(Set<Assumption> assumptions) {
        this.assumptions = assumptions;
    }

    public String getAnalysisPath() {
        return analysisPath;
    }

    public String getModelPath() {
        return modelPath;
    }

    public Set<Assumption> getAssumptions() {
        return assumptions;
    }
}
