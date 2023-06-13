package general;

import javafx.scene.control.Alert;

public class Utilities {
    public static void showAlertPopUp(Alert.AlertType type, String title, String header, String content){
        var alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
