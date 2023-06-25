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

import java.util.Set;

public class AnalysisConnector {
    public record AnalysisParameter(String modelPath, Set<Assumption> assumptions) {
    }

    private final Client client;
    private final String analysisUri;

    public AnalysisConnector(String analysisUri) {
        this.client = ClientBuilder.newClient();
        this.analysisUri = analysisUri;
    }

    public Pair<Integer, String> testConnection() {
        Pair<Integer, String> codeMessagePair;

        try (var response = this.client.target(this.analysisUri).path("test").request(MediaType.TEXT_PLAIN).get()) {
            codeMessagePair = new Pair<>(response.getStatus(), response.readEntity(String.class));
        } catch (IllegalArgumentException | NullPointerException e) {
            e.printStackTrace();
            codeMessagePair = new Pair<>(0, "The specified URI is invalid.");
        } catch (ProcessingException e) {
            e.printStackTrace();
            codeMessagePair = new Pair<>(0, "Connection to analysis could not be established.");
        }

        return codeMessagePair;
    }

    public Pair<Integer, String> performAnalysis(AnalysisParameter analysisParameter) {
        var objectMapper = new ObjectMapper();

        try {
            var jsonString = objectMapper.writeValueAsString(analysisParameter);

            try (var response = this.client.target(this.analysisUri).path("run").request().post(Entity.entity(jsonString, MediaType.APPLICATION_JSON))) {
                return new Pair<>(response.getStatus(), response.readEntity(String.class));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new Pair<>(0, "Marshalling failed due to malformed analysis parameters.");
        }
    }
}
