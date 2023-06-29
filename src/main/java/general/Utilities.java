package general;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Utilities {
    public static Optional<ButtonType> showAlert(@NotNull Alert.AlertType type,
                                                 @NotNull String title,
                                                 @NotNull String header,
                                                 @NotNull String content) {
        var alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait();
    }
}
