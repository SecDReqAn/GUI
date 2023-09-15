package io.securitycheck;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import general.entities.Assumption;
import general.entities.SecurityCheckAssumption;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import javafx.util.Pair;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class responsible for managing a connection to one analysis whose URI is specified on construction.
 */
public class SecurityAnalysisConnector {
    /**
     * Class {@link org.slf4j.Logger}.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAnalysisConnector.class);

    // REST API paths.
    private static final String PATH_CONNECTION_TEST = "test";
    private static final String PATH_ANALYSIS_EXECUTION = "run";
    private static final String PATH_MODEL_TRANSMISSION = "set/model/";

    /**
     * The data type that is sent to the analysis for the actual execution.
     *
     * @param modelPath   The path to the model to be analyzed.
     * @param assumptions The {@link Collection} of {@link Assumption}s for the analysis.
     */
    private record AnalysisParameter(String modelPath, Collection<Assumption> assumptions) {
    }

    /**
     * The data type that is received from the analysis after an execution.
     *
     * @param outputLog   The log produced by the analysis (e.g., the console output) or a potential error message.
     * @param assumptions The (potentially changed) {@link Assumption}s.
     */
    public record AnalysisOutput(String outputLog, Collection<SecurityCheckAssumption> assumptions) {
    }

    /**
     * The {@link Client} used for communicating with the analysis.
     */
    private final Client client;
    /**
     * The {@link ObjectMapper} used for marshalling / unmarshalling tasks.
     */
    private final ObjectMapper objectMapper;
    /**
     * The URI of the analysis represented as a {@link String}.
     */
    private final String analysisUri;

    /**
     * Constructs a new instance based on the specified URI-{@link String}.
     *
     * @param analysisUri The URI at which the analysis can be reached.
     */
    public SecurityAnalysisConnector(@Nullable String analysisUri) {
        this.client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
        this.analysisUri = analysisUri;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Tests the connection to the analysis.
     *
     * <p>
     * Note: Status code <code>0</code> represents a local processing error.
     * All other status codes are regular HTTP codes sent by the analysis.
     * </p>
     *
     * @return A {@link Pair} containing the status code (accessible via {@link Pair#getKey()}) and
     * message (accessible via {@link Pair#getValue()}) resulting from the connection test.
     */
    public @NotNull Pair<Integer, String> testConnection() {
        Pair<Integer, String> codeMessagePair;
        LOGGER.info("Testing connection to analysis with URI \"" + this.analysisUri + "\"");

        try (var response = this.client.target(this.analysisUri)
                .path(SecurityAnalysisConnector.PATH_CONNECTION_TEST)
                .request(MediaType.TEXT_PLAIN).get()) {
            codeMessagePair = new Pair<>(response.getStatus(), response.readEntity(String.class));
        } catch (IllegalArgumentException | NullPointerException e) {
            LOGGER.debug("Connection test to analysis with URI \"" + this.analysisUri
                                 + "\" failed due to malformed URI", e);
            codeMessagePair = new Pair<>(0, "The specified URI is invalid.");
        } catch (ProcessingException e) {
            LOGGER.debug("Connection test to analysis with URI \"" + this.analysisUri + "\" was unsuccessful", e);
            codeMessagePair = new Pair<>(0, "Connection to analysis could not be established.");
        }

        LOGGER.info("Connection test to analysis with URI \"" + this.analysisUri
                            + "\" resulted in code " + codeMessagePair.getKey());
        return codeMessagePair;
    }

    /**
     * Sends the specified data to the previously specified security analysis and initiates the actual analysis.
     * <p>
     * <b>Note</b>: Changes of the {@link Assumption}s caused by the security analysis (e.g., the <code>analyzed</code>
     * field) will be reflected in the {@link Assumption}s specified by the <code>assumptions</code> parameter.
     * <br/>
     * <b>Note</b>: The {@link Assumption}s specified via <code>assumptions</code> are not sent to the security analysis
     * in full. Instead, {@link AssumptionViews.SecurityCheckAnalysisView} (cf. annotations in {@link Assumption})
     * is used to only serialize the necessary fields.
     * </p>
     *
     * @param modelPath   The path to the PCM model that should be used for the analysis.
     * @param assumptions The {@link Collection} of {@link Assumption}s that should be used for the analysis.
     * @return A {@link Pair} encompassing a status code accessible via {@link Pair#getKey()} and the output log of the
     * analysis accessible via {@link Pair#getValue()}.
     */
    public Pair<Integer, AnalysisOutput> performAnalysis(@NotNull String modelPath,
                                                         @NotNull Collection<Assumption> assumptions) {
        LOGGER.info("Performing analysis on model with path \"" + modelPath + "\" with analysis \""
                            + this.analysisUri + "\"");

        try {
            var jsonString = this.objectMapper.writerWithView(AssumptionViews.SecurityCheckAnalysisView.class)
                    .writeValueAsString(new AnalysisParameter(modelPath, assumptions));

            try (var response = this.client.target(this.analysisUri)
                    .path(SecurityAnalysisConnector.PATH_ANALYSIS_EXECUTION)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.entity(jsonString, MediaType.APPLICATION_JSON))) {
                String jsonResponse = response.readEntity(String.class);

                var codeMessagePair = new Pair<>(response.getStatus(), this.objectMapper.readValue(jsonResponse,
                                                                                                   AnalysisOutput.class));
                LOGGER.info("Analysis execution on security analysis \"" + this.analysisUri
                                    + "\" resulted in code " + codeMessagePair.getKey());
                return codeMessagePair;
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("Error due to malformed analysis parameters when trying to execute analysis", e);
            return new Pair<>(0, new AnalysisOutput("Marshalling failed due to malformed analysis parameters.", null));
        } catch (ProcessingException e) {
            LOGGER.error("Error due to processing exception when trying to execute analysis", e);
            return new Pair<>(0, new AnalysisOutput("Processing Exception: " + e.getMessage(), null));
        }
    }

    /**
     * Transfers the model-view-files locates in the specified model folder to the analysis.
     *
     * @param modelPath The {@link File} specifying the folder containing the model-view-files.
     * @return A {@link Pair} encompassing a status code accessible via {@link Pair#getKey()} and a potential status
     * message accessible via {@link Pair#getValue()}.
     */
    public Pair<Integer, String> transferModelFiles(@NotNull File modelPath) {
        // Determine list of files that are part of the model and must be transferred to the analysis.
        var filesInModelFolder = modelPath.listFiles();

        if (filesInModelFolder == null || filesInModelFolder.length == 0) {
            // Abort.
            return new Pair<>(0, "The model could not be transmitted to the analysis as there are no files contained " +
                    "in the specified model folder.");
        }

        // Do not consider folders and nested / hidden files.
        var relevantFiles = Stream.of(filesInModelFolder)
                .filter(File::isFile)
                .filter(file -> !file.getName().startsWith("."))
                .collect(Collectors.toSet());

        try (var multiPart = new FormDataMultiPart()) {
            for (var file : relevantFiles) {
                multiPart.bodyPart(new FileDataBodyPart(file.getName(), file));
            }

            LOGGER.info("Transferring model with path \"" + modelPath.getAbsolutePath() + "\" to analysis with URI " +
                                "\"" + this.analysisUri + "\"");
            try (var response = this.client.target(this.analysisUri)
                    .path(SecurityAnalysisConnector.PATH_MODEL_TRANSMISSION + modelPath.getName())
                    .request().post(Entity.entity(multiPart, multiPart.getMediaType()))) {
                var codeMessagePair = new Pair<>(response.getStatus(), response.readEntity(String.class));
                LOGGER.info("Model transfer to analysis with URI \"" + this.analysisUri + "\" resulted in code "
                                    + codeMessagePair.getKey());
                return codeMessagePair;
            }
        } catch (IOException e) {
            LOGGER.error("Error creating multi-part object for model transfer", e);
            return new Pair<>(0, "Could not create a multi-part object for sending the model to the analysis.");
        }

    }
}
