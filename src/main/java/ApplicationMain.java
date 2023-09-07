import controllers.MainScreenController;
import general.Constants;
import io.assumptiongraph.AssumptionGraphServerConfig;
import jakarta.ws.rs.core.UriBuilder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Conceptual entry point of Assumption Analyzer.
 *
 * <p><b>Note</b>: To start Assumption Analyzer, {@link Main} should be executed instead. This avoids problems with
 * regard to the application's module path</p>
 */
public class ApplicationMain extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationMain.class);

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("UI/MainScreen.fxml"));

        var scene = new Scene(loader.load());
        var mainScreenController = (MainScreenController) loader.getController();
        mainScreenController.setHostServices(this.getHostServices());

        LOGGER.info("Successfully initialized main scene and associated controller.");

        // Init AssumptionGraph API
        var assumptionGraphUri = UriBuilder.fromUri("http://localhost/").port(2407).build();
        var assumptionGrapjServer = JettyHttpContainerFactory.createServer(assumptionGraphUri,
                new AssumptionGraphServerConfig(mainScreenController));
        assumptionGrapjServer.setStopAtShutdown(true);

        try {
            assumptionGrapjServer.start();
            LOGGER.info("Successfully started the AnalysisGraph API server");
        } catch (Exception e) {
            LOGGER.error("Failed to start the AnalysisGraph API server: " + e.getMessage());
        }

        // Initialize stage.
        stage.setTitle(Constants.DEFAULT_STAGE_TITLE);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(windowEvent -> {
            mainScreenController.handleExitRequest();
            try {
                assumptionGrapjServer.stop();
                LOGGER.info("Successfully stopped the AnalysisGraph API server");
            } catch (Exception e) {
                LOGGER.error("Failed to stop the AnalysisGraph API server: " + e.getMessage());
            } finally {
                assumptionGrapjServer.destroy();
            }
        });
    }

    public static void main(String[] args) {
        Application.launch();
    }
}
