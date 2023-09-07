package io.assumptiongraph;

import controllers.MainScreenController;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * The JAX-RS resource that responds to all AssumptionGraph API requests.
 */
@Path("/assumption-graph")
public class AssumptionGraphResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(AssumptionGraphResource.class);
    /**
     * The {@link MainScreenController} used to retrieve the current {@link general.entities.Configuration} of
     * Assumption Analyzer.
     */
    @Inject
    private MainScreenController mainScreenController;

    /**
     * The <code>/assumption-graph/assumptions</code> endpoint used for retrieving the currently
     * active {@link general.entities.Assumption} set.
     *
     * @param headers The {@link HttpHeaders} of the request.
     * @return A {@link Response} containing the {@link general.entities.Assumption}s serialized as JSON.
     */
    @GET
    @Path("/assumptions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFullAssumptionSet(@Context HttpHeaders headers) {
        Collection<String> hostStrings = headers.getRequestHeader("host");
        LOGGER.info("Received request for assumption set from host \""
                + (hostStrings == null ? "unknown" : hostStrings) + "\"");

        var assumptions = this.mainScreenController.getCurrentConfig().getAssumptions();
        return Response.ok(assumptions).build();
    }

    /**
     * The <code>/assumption-graph/results</code> endpoint used for retrieving the currently
     * active {@link general.entities.AnalysisResult} set.
     *
     * @param headers The {@link HttpHeaders} of the request.
     * @return A {@link Response} containing the {@link general.entities.AnalysisResult}s serialized as JSON.
     */
    @GET
    @Path("/results")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSecurityAnalysisResults(@Context HttpHeaders headers) {
        Collection<String> hostStrings = headers.getRequestHeader("host");
        LOGGER.info("Received request for security analysis result set from host \""
                + (hostStrings == null ? "unknown" : hostStrings) + "\"");

        var securityAnalysisResults = this.mainScreenController.getCurrentConfig().getAnalysisResults();
        return Response.ok(securityAnalysisResults).build();
    }

    /**
     * The <code>/assumption-graph/model-path</code> endpoint used for retrieving the currently
     * active model path (i.e., the local absolute path to the selected PCM model).
     *
     * @param headers The {@link HttpHeaders} of the request.
     * @return A {@link Response} containing the local absolute path to the PCM model as plain text.
     */
    @GET
    @Path("/model-path")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getModelPath(@Context HttpHeaders headers) {
        Collection<String> hostStrings = headers.getRequestHeader("host");
        LOGGER.info("Received request for model path from host \""
                + (hostStrings == null ? "unknown" : hostStrings) + "\"");

        var modelPath = this.mainScreenController.getCurrentConfig().getModelPath();
        return Response.ok(modelPath).build();
    }

    /**
     * The <code>/assumption-graph/security-analysis</code> endpoint used for retrieving the currently
     * active security analysis URI.
     *
     * @param headers The {@link HttpHeaders} of the request.
     * @return A {@link Response} containing the currently used URI to the security analysis as plain text.
     */
    @GET
    @Path("/security-analysis")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getSecurityAnalysisUri(@Context HttpHeaders headers) {
        Collection<String> hostStrings = headers.getRequestHeader("host");
        LOGGER.info("Received request for security analysis uri from host \""
                + (hostStrings == null ? "unknown" : hostStrings) + "\"");

        var analysisUri = this.mainScreenController.getCurrentConfig().getAnalysisPath();
        return Response.ok(analysisUri).build();
    }
}
