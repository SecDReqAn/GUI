package network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import general.Assumption;
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

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnalysisConnector {
    public record AnalysisParameter(String modelPath, Set<Assumption> assumptions) {
    }

    private final Client client;
    private final ObjectMapper objectMapper;
    private final String analysisUri;

    public AnalysisConnector(String analysisUri) {
        this.client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
        this.analysisUri = analysisUri;
        this.objectMapper = new ObjectMapper();
    }

    public Pair<Integer, String> testConnection() {
        Pair<Integer, String> codeMessagePair;

        try (var response = this.client.target(this.analysisUri).path("test").request(MediaType.TEXT_PLAIN).get()) {
            codeMessagePair = new Pair<>(response.getStatus(), response.readEntity(String.class));
        } catch (IllegalArgumentException | NullPointerException e) {
            codeMessagePair = new Pair<>(0, "The specified URI is invalid.");
        } catch (ProcessingException e) {
            codeMessagePair = new Pair<>(0, "Connection to analysis could not be established.");
        }

        return codeMessagePair;
    }

    public Pair<Integer, String> performAnalysis(AnalysisParameter analysisParameter) {
        try {
            var jsonString = this.objectMapper.writeValueAsString(analysisParameter);

            try (var response = this.client.target(this.analysisUri).path("run").request().post(Entity.entity(jsonString, MediaType.APPLICATION_JSON))) {
                return new Pair<>(response.getStatus(), response.readEntity(String.class));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Pair<>(0, "Marshalling failed due to malformed analysis parameters.");
        }
    }

    public Pair<Integer, String> transferModelFiles(@NotNull File modelPath) {
        // Determine list of files that are part of the model and must be transferred to the analysis.
        var filesInModelFolder = modelPath.listFiles();

        if (filesInModelFolder == null || filesInModelFolder.length == 0) {
            // Abort.
            return new Pair<>(0, "The model could not be transmitted to the analysis as there are no files contained in the specified model folder.");
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

            try(var response = this.client.target(this.analysisUri).path("set/model").request().post(Entity.entity(multiPart, multiPart.getMediaType()))){
                return new Pair<>(response.getStatus(), response.readEntity(String.class));
            }
        } catch (IOException e) {
            return new Pair<>(0, "Could not create a multi-part object for sending the model to the analysis.");
        }
    }
}
