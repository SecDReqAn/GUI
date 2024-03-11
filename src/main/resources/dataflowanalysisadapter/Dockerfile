# This Dockerfile is designed to build a container for the DataFlowAnalysis microservice

# Specifies the base image as OpenJDK 17, which includes the Java runtime.
FROM openjdk:20

# Copies the compiled class files and resources from the local "MyDataFlowAnalysisDependencyDirectory" directory into 
# the container under "DataFlowAnalysisDependencies". 
# Please make sure to replace "MyDataFlowAnalysisDependencyDirectory" with the actual directory containing your dependencies before building the Docker image.
COPY MyDataFlowAnalysisDependencyDirectory DataFlowAnalysisDependencies

# Specifies the command that will be executed when the container starts. In this case, it runs the Java application with the specified classpath and options. 
# The main class is "rest.DataFlowAnalysisConnector".
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-classpath", "DataFlowAnalysisDependencies/DataFlowAnalysis/bundles/org.palladiosimulator.dataflow.confidentiality.analysis/bin:DataFlowAnalysisDependencies/DataFlowAnalysis/tests/org.palladiosimulator.dataflow.confidentiality.analysis.tests/target/classes:DataFlowAnalysisDependencies/DataFlowAnalysis/tests/org.palladiosimulator.dataflow.confidentiality.analysis.testmodels/bin:DataFlowAnalysisDependencies/external_dependency_jars/*", "-XX:+ShowCodeDetailsInExceptionMessages", "rest.DataFlowAnalysisConnector"]
