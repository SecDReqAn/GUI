import controllers.MainScreenController;
import general.Constants;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ApplicationMain extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("UI/MainScreen.fxml"));

        var scene = new Scene(loader.load());
        var mainScreenController = (MainScreenController) loader.getController();
        mainScreenController.setHostServices(this.getHostServices());

        stage.setTitle(Constants.DEFAULT_STAGE_TITLE);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(windowEvent -> mainScreenController.handleExitRequest(true));
    }

    public static void main(String[] args) {
        Application.launch();
    }
}
