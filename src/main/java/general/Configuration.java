package general;

import java.util.Set;

// TODO Add field for the result of an analysis.

public record Configuration(String analysisPath, String modelPath, Set<Assumption> assumptions, String analysisResult) {
    @Override
    public String toString() {
        return "Configuration{" +
                "analysisPath='" + analysisPath + '\'' +
                ", modelPath='" + modelPath + '\'' +
                ", assumptions=" + assumptions +
                ", analysisResult='" + analysisResult + '\'' +
                '}';
    }
}
