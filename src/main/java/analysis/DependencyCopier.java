package analysis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Utility program that copies all Abunai dependencies (extracted from a specified classpath)
 * into a specified directory.
 */
public class DependencyCopier {

    private static final String EXTERNAL_JAR_DIR_NAME = "external_dependency_jars";
    private static final String ABUNAI_DIR_NAME = "UncertaintyImpactAnalysis";
    private static final String PALLADIO_DFCA_DIR_NAME = "Palladio-Addons-DataFlowConfidentiality-Analysis";

    private static void copyContentsOfDirectory(File srcDir, File destDir) {
        try {
            Files.walk(srcDir.toPath()).forEach(srcPath -> {
                // TODO Complete
                Path destPath = Paths.get(destDir, srcPath.toString().substring(srcDir.length()));

                try {
                    Files.copy(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            System.out.println("Error copying directory " + srcDir.getAbsolutePath() + " to " + destDir.getAbsolutePath());
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Error: Expected two arguments (absolute path to target folder, classpath parameter)");
            return;
        }

        String absoluteTargetPath = args[0];
        String classpathString = args[1];

        var targetDirectory = new File(absoluteTargetPath);
        if (!targetDirectory.exists() || !targetDirectory.isDirectory()) {
            System.out.println("Error: Invalid input for absolute path to target folder");
            return;
        }

        // Create base folders for the dependencies.
        new File(targetDirectory.getAbsolutePath() + File.separator + DependencyCopier.EXTERNAL_JAR_DIR_NAME).mkdir();
        new File(targetDirectory.getAbsolutePath() + File.separator + DependencyCopier.ABUNAI_DIR_NAME).mkdir();
        new File(targetDirectory.getAbsolutePath() + File.separator + DependencyCopier.PALLADIO_DFCA_DIR_NAME).mkdir();

        String[] classpathDependencies = classpathString.split(":");
        for (String dependencyPath : classpathDependencies) {
            if (dependencyPath.endsWith(".jar")) { // Handle external dependencies.
                var jarFile = new File(dependencyPath);
                var targetJarFile = new File(absoluteTargetPath + File.separator + DependencyCopier.EXTERNAL_JAR_DIR_NAME + jarFile.getName());

                try {
                    Files.copy(jarFile.toPath(), targetJarFile.toPath());
                } catch (Exception e) {
                    System.out.println("Warning: Copying " + jarFile.getName() + " failed.");
                }
            } else { // Handle internal dependencies (i.e. class files).
                if(dependencyPath.contains(DependencyCopier.ABUNAI_DIR_NAME)){
                    int relevantSubStringStart = dependencyPath.indexOf(DependencyCopier.ABUNAI_DIR_NAME);
                    String relevantSubString = dependencyPath.substring(relevantSubStringStart);
                    File targetInnerDir = new File(targetDirectory.getAbsolutePath() + File.separator + relevantSubString);
                    // TODO
                } else if (dependencyPath.contains(DependencyCopier.PALLADIO_DFCA_DIR_NAME)) {
                    // TODO
                } else {
                    System.out.println("Warning: Unknown dependency: " + dependencyPath);
                }
            }
        }
    }

}
