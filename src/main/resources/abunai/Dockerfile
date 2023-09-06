# Use a base image with a Java runtime.
FROM openjdk:17

# Copy compiled class files and resources into container.
COPY MyAbunaiDependencyDirectory AbunaiDependencies

# Set the main class and the runtime arguments.
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-classpath", "AbunaiDependencies/Palladio-Addons-DataFlowConfidentiality-Analysis/bundles/org.palladiosimulator.dataflow.confidentiality.analysis/bin:AbunaiDependencies/UncertaintyImpactAnalysis/bundles/dev.abunai.impact.analysis/bin:AbunaiDependencies/UncertaintyImpactAnalysis/tests/dev.abunai.impact.analysis.tests/target/classes:AbunaiDependencies/UncertaintyImpactAnalysis/tests/dev.abunai.impact.analysis.testmodels/bin:AbunaiDependencies/external_dependency_jars/*", "-XX:+ShowCodeDetailsInExceptionMessages", "rest.AbunaiConnector"]