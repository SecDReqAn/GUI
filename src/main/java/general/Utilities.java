package general;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

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

    public static ContextMenu createSingleContextMenu(String prompt, Consumer<ActionEvent> actionHandling){
        var menuItem = new MenuItem(prompt);
        menuItem.setOnAction(actionHandling::accept);

        var contextMenu = new ContextMenu();
        contextMenu.getItems().add(menuItem);

        return contextMenu;
    }

    public static void enableTextWrapForTableColumn(TableColumn<Assumption, String> column){
        column.setCellFactory(tc -> {
            var tableCell = new TableCell<Assumption, String>();
            var text = new Text();
            tableCell.setGraphic(text);
            tableCell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(column.widthProperty());
            text.textProperty().bind(tableCell.itemProperty());
            return tableCell;
        });
    }
}
