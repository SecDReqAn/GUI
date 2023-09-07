package io.assumptiongraph;

import controllers.MainScreenController;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/assumption-graph")
public class AssumptionGraphResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(AssumptionGraphResource.class);

    @Inject
    private MainScreenController mainScreenController;
    @GET
    @Path("/assumptions")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFullAssumptionSet(){
        LOGGER.info("Received request for assumption set");

        // TODO Complete
        return "Assumption Test Worked --> Number of Assumptions: " + mainScreenController.getCurrentConfig().getAssumptions().size();
    }

    // TODO Add remaining endpoints.
}
