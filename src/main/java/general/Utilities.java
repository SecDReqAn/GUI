package general;

import general.entities.GraphAssumption;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Convenience class providing various utility functions (mostly with regard to JavaFX controls).
 */
public class Utilities {
    /**
     * Convenience function that shows an {@link Alert} to the user with the specified attributes.
     *
     * @param type        The {@link javafx.scene.control.Alert.AlertType} that should be used.
     * @param title       The title {@link String} that should be displayed.
     * @param header      The header {@link String} that should be displayed.
     * @param content     The content {@link String} that should be displayed.
     * @param buttonTypes The {@link ButtonType}s that should be used with the {@link Alert}.
     * @return The {@link Optional} of {@link ButtonType} returned by {@link Alert#showAndWait()}.
     */
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

    /**
     * Convenience function that adds a new context menu item to the specified {@link Control} element.
     *
     * @param controlElement The {@link Control} element to which the context menu item should be added.
     * @param prompt         The prompt text that gets displayed for the item in the context menu.
     * @param callback       A {@link Consumer} specifying a callback function that should be called on selection of
     *                       the new context menu item.
     */
    public static void addFunctionalityToContextMenu(@NotNull Control controlElement,
                                                     @NotNull String prompt,
                                                     @NotNull Consumer<ActionEvent> callback) {
        var menuItem = new MenuItem(prompt);
        menuItem.setOnAction(callback::accept);

        if (controlElement.getContextMenu() == null) {
            var contextMenu = new ContextMenu();
            contextMenu.getItems().add(menuItem);
            controlElement.setContextMenu(contextMenu);
        } else {
            controlElement.getContextMenu().getItems().add(menuItem);
        }

    }

    /**
     * Convenience function that adds a new {@link SeparatorMenuItem} to the {@link ContextMenu} of the specified
     * {@link Control} element.
     *
     * @param controlElement The {@link Control} element to whose {@link ContextMenu} the {@link SeparatorMenuItem}
     *                       should be added.
     */
    public static void addSeparatorToContextMenu(@NotNull Control controlElement) {
        controlElement.getContextMenu().getItems().add(new SeparatorMenuItem());
    }

    /**
     * Convenience function that enables text-wrap for the specified {@link TableColumn}.
     *
     * @param column The {@link TableColumn} (containing {@link GraphAssumption}s and displaying {@link String}s) for
     *               which text-wrap should be enabled.
     */
    public static void enableTextWrapForTableColumn(@NotNull TableColumn<GraphAssumption, @NotNull String> column) {
        column.setCellFactory(tc -> {
            var tableCell = new TableCell<GraphAssumption, String>();
            var text = new Text();

            tableCell.setGraphic(text);
            tableCell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(column.widthProperty());
            text.textProperty().bind(tableCell.itemProperty());
            text.getStyleClass().add("text-node");

            return tableCell;
        });
    }

    /**
     * Convenience function that adds a custom cell value factory to the specified {@link TableColumn}, which
     * transforms the contained {@link GraphAssumption}s according to the specified callback {@link Function}.
     *
     * @param column                 The {@link TableColumn} (containing {@link GraphAssumption}s and displaying
     *                               {@link String}s) whose cell value factory should be set.
     * @param transformationCallback The callback {@link Function} that should be used to transform
     *                               {@link GraphAssumption}s into the {@link String}s displayed in the {@link TableColumn}.
     */
    public static void setCellValueFactoryForAssumptionColumn(@NotNull TableColumn<GraphAssumption, @NotNull String> column,
                                                              @NotNull Function<GraphAssumption, String> transformationCallback) {
        column.setCellValueFactory(cellData -> {
            GraphAssumption assumption = cellData.getValue();
            return new ReadOnlyStringWrapper(transformationCallback.apply(assumption));
        });
    }

    /**
     * Convenience function that retrieves the {@link Stage} associated with a given {@link MenuItem}.
     *
     * @param menuItem The {@link MenuItem} whose associated {@link Stage} should be retrieved.
     * @return The associated {@link Stage}.
     */
    public static @NotNull Stage getStageOfMenuItem(@NotNull MenuItem menuItem) {
        return (Stage) menuItem.getParentPopup().getOwnerWindow();
    }

    /**
     * Convenience function that compares two strings. This comparison considers null values
     * and uses lexicographic ordering for non-null values.
     *
     * @param s1 The first {@link String} for the comparison.
     * @param s2 The second {@link String} for the comparison.
     * @return A negative integer if {@code s1} is non-null and {@code s2} is null,
     * a positive integer if {@code s1} is null and {@code s2} is non-null,
     * the result of lexicographic comparison of {@code s1} and {@code s2}
     * if both are non-null, or 0 if both are null.
     */
    public static int compareStrings(@Nullable String s1, @Nullable String s2) {
        if (s1 != null && s2 == null) {
            // s2 null.
            return -1;
        } else if (s1 == null && s2 != null) {
            // s1 null.
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
