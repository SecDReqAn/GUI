package analysis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

/**
 * Utility program that copies all Abunai dependencies (extracted from a specified classpath)
 * into a specified directory.
 */
public class AbunaiDependencyCopier {

    private static final String EXTERNAL_JAR_DIR_NAME = "external_dependency_jars";
    private static final String ABUNAI_DIR_NAME = "UncertaintyImpactAnalysis";
    private static final String PALLADIO_DFCA_DIR_NAME = "Palladio-Addons-DataFlowConfidentiality-Analysis";

    private static void copyContentsOfDirectory(File srcDir, File destDir) {
        try (Stream<Path> pathStream = Files.walk(srcDir.toPath())) {
            pathStream.forEach(srcPath -> {
                Path destPath = Paths.get(destDir.getAbsolutePath(),
                        srcPath.toString().substring(srcDir.getAbsolutePath().length()));

                try {
                    Files.copy(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.out.println("Error copying file " + srcPath + " to " + destDir.getAbsolutePath());
                }
            });
        } catch (IOException e) {
            System.out.println("Error copying directory " + srcDir.getAbsolutePath()
                    + " to " + destDir.getAbsolutePath());
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Error: Expected two arguments (absolute path to target folder, classpath parameter)");
            return;
        }

        String absoluteTargetPath = args[0];
        String classpathString = args[1];

        System.out.println("Trying to copy dependencies of classpath \"" + classpathString + "\"\nto target directory: " + absoluteTargetPath);

        var targetDirectory = new File(absoluteTargetPath);
        if (!targetDirectory.exists() || !targetDirectory.isDirectory()) {
            System.out.println("Error: Invalid input for absolute path to target folder");
            return;
        }

        // Create base folders for the dependencies.
        var jarDirectory = new File(targetDirectory.getAbsolutePath()
                + File.separator + AbunaiDependencyCopier.EXTERNAL_JAR_DIR_NAME);
        if (!jarDirectory.mkdir()) {
            System.out.println("Error: Failed to create " + jarDirectory.getAbsolutePath() + " directory!");
            return;
        }

        var abunaiDirectory = new File(targetDirectory.getAbsolutePath()
                + File.separator + AbunaiDependencyCopier.ABUNAI_DIR_NAME);
        if (!abunaiDirectory.mkdir()) {
            System.out.println("Error: Failed to create " + jarDirectory.getAbsolutePath() + " directory!");
            return;
        }

        // Create case-studies directory for the PCM models.
        var casestudiesDir = new File(abunaiDirectory.getAbsolutePath() + File.separator
                + "tests" + File.separator
                + "dev.abunai.impact.analysis.testmodels" + File.separator
                + "casestudies");
        if (!casestudiesDir.mkdirs()) {
            System.out.println("Error: Failed to create " + casestudiesDir + " directory!");
            return;
        }

        var palladioDFCADirectory = new File(targetDirectory.getAbsolutePath()
                + File.separator + AbunaiDependencyCopier.PALLADIO_DFCA_DIR_NAME);
        if (!palladioDFCADirectory.mkdir()) {
            System.out.println("Error: Failed to create " + jarDirectory.getAbsolutePath() + " directory!");
            return;
        }

        String[] classpathDependencies = classpathString.split(":");
        for (String dependencyPath : classpathDependencies) {
            if (dependencyPath.endsWith(".jar")) { // Handle external dependencies.
                var jarFile = new File(dependencyPath);
                var targetJarFile = new File(jarDirectory + File.separator + jarFile.getName());

                try {
                    Files.copy(jarFile.toPath(), targetJarFile.toPath());
                } catch (Exception e) {
                    System.out.println("Warning: Copying " + jarFile.getName() + " failed.");
                }
            } else { // Handle internal dependencies (i.e. class files).
                // Assuming that one of the dependencies does not contain both ABUNAI_DIR_NAME and PALLADIO_DFCA_DIR_NAME.
                int abunaiSuffixStartIndex = dependencyPath.indexOf(AbunaiDependencyCopier.ABUNAI_DIR_NAME);
                int palladioDFCASuffixStartIndex = dependencyPath.indexOf(AbunaiDependencyCopier.PALLADIO_DFCA_DIR_NAME);

                int suffixStartIndex = Math.max(abunaiSuffixStartIndex, palladioDFCASuffixStartIndex);
                if (suffixStartIndex != -1) {
                    String subDirectoryPath = dependencyPath.substring(suffixStartIndex);

                    // Copy contents of directory.
                    var srcDir = new File(dependencyPath);
                    var targetSubDir = new File(targetDirectory + File.separator + subDirectoryPath);
                    if (!targetSubDir.mkdirs()) {
                        System.out.println("Warning: Failed to create " + jarDirectory.getAbsolutePath() +
                                " directory for dependency " + dependencyPath + ". Skipping this dependency!");
                        continue;
                    }

                    AbunaiDependencyCopier.copyContentsOfDirectory(srcDir, targetSubDir);
                } else {
                    System.out.println("Warning: Unknown dependency: " + dependencyPath);
                }
            }
        }

        System.out.println("Successfully copied Abunai dependencies to \"" + absoluteTargetPath + "\"");
    }

}
