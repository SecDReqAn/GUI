package io.assumptiongraph;

import com.fasterxml.jackson.core.util.JacksonFeature;
import controllers.MainScreenController;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/")
public class AssumptionGraphServerConfig extends ResourceConfig {
    /**
     * Constructs a new {@link AssumptionGraphServerConfig} instance with the specified {@link MainScreenController} to
     * inject.
     *
     * @param mainScreenController The {@link MainScreenController} which should be injected into the
     *                             {@link AssumptionGraphResource}.
     */
    public AssumptionGraphServerConfig(MainScreenController mainScreenController) {
        // Enable JSON.
        register(JacksonFeature.class);
        // Register resource.
        register(AssumptionGraphResource.class);
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(mainScreenController);
            }
        });
    }
}
