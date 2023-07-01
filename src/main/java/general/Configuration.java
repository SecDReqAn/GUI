package general;

import java.util.Set;

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
