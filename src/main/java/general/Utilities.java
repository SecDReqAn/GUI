package general;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class Utilities {
    public static @NotNull Optional<ButtonType> showAlert(@NotNull Alert.AlertType type,
                                                          @NotNull String title,
                                                          @NotNull String header,
                                                          @NotNull String content,
                                                          @NotNull ButtonType... buttonTypes) {
        var alert = new Alert(type, content, buttonTypes);
        alert.setTitle(title);
        alert.setHeaderText(header);
        return alert.showAndWait();
    }

    public static void addFunctionalityToContextMenu(@NotNull Control controlElement, @NotNull String prompt, @NotNull Consumer<ActionEvent> actionHandling) {
        var menuItem = new MenuItem(prompt);
        menuItem.setOnAction(actionHandling::accept);

        if (controlElement.getContextMenu() == null) {
            var contextMenu = new ContextMenu();
            contextMenu.getItems().add(menuItem);
            controlElement.setContextMenu(contextMenu);
        } else {
            controlElement.getContextMenu().getItems().add(menuItem);
        }

    }

    public static void enableTextWrapForTableColumn(@NotNull TableColumn<Assumption, String> column) {
        column.setCellFactory(tc -> {
            var tableCell = new TableCell<Assumption, String>();
            var text = new Text();
            tableCell.setGraphic(text);
            tableCell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(column.widthProperty());
            text.textProperty().bind(tableCell.itemProperty());
            text.getStyleClass().add("text-node");

            return tableCell;
        });
    }

    public static void setCellValueFactoryForCollectionElement(@NotNull TableColumn<Assumption, String> column, @NotNull Function<Assumption, String> transformation) {
        column.setCellValueFactory(cellData -> {
            Assumption assumption = cellData.getValue();
            return new ReadOnlyStringWrapper(transformation.apply(assumption));
        });
    }

    /**
     * Utility function that retrieves the {@link Stage} associated with a given {@link MenuItem}.
     *
     * @param menuItem The {@link MenuItem} whose associated {@link Stage} should be retrieved.
     * @return The associated {@link Stage}.
     */
    public static @NotNull Stage getStageOfMenuItem(@NotNull MenuItem menuItem) {
        return (Stage) menuItem.getParentPopup().getOwnerWindow();
    }
}
