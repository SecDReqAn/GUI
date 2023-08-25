package general;

import general.entities.Assumption;
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
import org.jetbrains.annotations.Nullable;

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

    /**
     * Compares two strings for order. This comparison considers null values
     * and uses lexicographic ordering for non-null values.
     *
     * @param s1 The first {@link String} for the comparison.
     * @param s2 The second {@link String} for the comparison.
     * @return A negative integer if {@code s1} is non-null and {@code s2} is null,
     *         a positive integer if {@code s1} is null and {@code s2} is non-null,
     *         the result of lexicographic comparison of {@code s1} and {@code s2}
     *         if both are non-null, or 0 if both are null.
     */
    public static int compareStrings(@Nullable String s1, @Nullable String s2) {
        if (s1 != null && s2 == null) {
            // Second null.
            return -1;
        } else if (s1 == null && s2 != null) {
            // First null.
            return 1;
        } else if (s1 != null) {
            // Both not null.
            return s1.compareTo(s2);
        } else {
            // Both null.
            return 0;
        }
    }
}
