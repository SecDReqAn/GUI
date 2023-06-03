package general;

import java.util.Set;

public class Configuration {
    private String analysisPath;
    private String modelName;
    private Set<Assumption> assumptions;

    public Configuration(String analysisPath, String modelName, Set<Assumption> assumptions){
        this.analysisPath = analysisPath;
        this.modelName = modelName;
        this.assumptions = assumptions;
    }

    public String getAnalysisPath() {
        return analysisPath;
    }

    public String getModelName() {
        return modelName;
    }

    public Set<Assumption> getAssumptions() {
        return assumptions;
    }
}
