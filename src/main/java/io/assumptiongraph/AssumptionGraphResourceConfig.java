package io.assumptiongraph;

import com.fasterxml.jackson.core.util.JacksonFeature;
import controllers.MainScreenController;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Custom {@link ResourceConfig} that configures the {@link AssumptionGraphResource}.
 */
@ApplicationPath("/")
public class AssumptionGraphResourceConfig extends ResourceConfig {
    /**
     * Constructs a new {@link AssumptionGraphResourceConfig} instance with the specified
     * {@link MainScreenController} to inject.
     *
     * @param mainScreenController The {@link MainScreenController} which should be injected into the
     *                             {@link AssumptionGraphResource}.
     */
    public AssumptionGraphResourceConfig(MainScreenController mainScreenController) {
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
