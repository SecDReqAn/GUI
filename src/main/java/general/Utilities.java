package general;

import javafx.scene.control.Alert;

public class Utilities {
    public static void showAlertPopUp(Alert.AlertType type, String title, String header, String content){
        var alert = new Alert(type);
        alert.setTitle("Warning");
        alert.setHeaderText("Unable to create a new assumption!");
        alert.setContentText("A path to a valid model and analysis first has to be set.");
        alert.showAndWait();
    }
}
