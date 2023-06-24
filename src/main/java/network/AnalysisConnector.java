package network;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import javafx.util.Pair;

public class AnalysisConnector {
    private final Client client;
    private final String analysisUri;

    public static void main(String[] args) {
        AnalysisConnector testConnector = new AnalysisConnector("http://localhost:2406/abunai");
        //testConnector.executeGetTest();
        System.out.println(testConnector.testConnection());
    }

    public AnalysisConnector(String analysisUri) {
        this.client = ClientBuilder.newClient();
        this.analysisUri = analysisUri;
    }

    public Pair<Integer, String> setModelPath(String path) {
        var response = this.client.target(this.analysisUri).path("model")
                .request().put(Entity.entity(path, MediaType.TEXT_PLAIN));

        var codeMessagePair = new Pair<>(response.getStatus(), response.readEntity(String.class));
        response.close();
        return codeMessagePair;
    }

    public Pair<Integer, String> testConnection() {
        Pair<Integer, String> codeMessagePair;

        try (var response = this.client.target(this.analysisUri).path("test").request(MediaType.TEXT_PLAIN).get()){
            codeMessagePair = new Pair<>(response.getStatus(), response.readEntity(String.class));

        } catch (IllegalArgumentException | NullPointerException e){
            codeMessagePair = new Pair<>(0, "The specified URI is invalid.");
        } catch(ProcessingException e){
            codeMessagePair = new Pair<>(0, "Connection to analysis could not be established.");
        }

        return codeMessagePair;
    }
}
