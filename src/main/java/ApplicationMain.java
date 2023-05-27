import controllers.MainScreenController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ApplicationMain extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("UI/MainScreen.fxml"));

        VBox vbox = loader.load();
        ((MainScreenController) loader.getController()).setHostServices(this.getHostServices());

        Scene scene = new Scene(vbox);
        stage.setTitle("Assumption Specifier");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch();
    }
}
