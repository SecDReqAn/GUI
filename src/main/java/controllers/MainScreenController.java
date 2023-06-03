package controllers;

import general.Assumption;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MainScreenController {
    private final String defaultSaveLocation;
    private HostServices hostServices;
    private File saveFile;
    private String analysis = "";

    @FXML
    private ListView<Assumption> assumptions;

    public MainScreenController() {
        this.defaultSaveLocation = System.getProperty("user.home") + System.getProperty("file.separator") + "NewAssumptionSet.asf";
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    private ObservableList<String> getAvailableAnalyses() {
        // TODO Determine the available analyses.
        return FXCollections.observableArrayList("Abunai", "Analysis 2", "Analysis 3");
    }

    @FXML
    private void handleNewAssumption(ActionEvent actionEvent) {
        if (this.analysis.isEmpty()) {
            var alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Unable to create a new assumption!");
            alert.setContentText("An analysis has to be selected before creating a new assumption.");

            alert.showAndWait();
            return;
        }

        // Create new modal window for entry of assumption parameters.
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../UI/AssumptionSpecificationScreen.fxml"));
            AnchorPane root = loader.load();

            AssumptionSpecificationScreenController controller = loader.getController();
            Assumption newAssumption = new Assumption();
            controller.initAssumption(newAssumption);

            var stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Assumption Specification");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((MenuItem) actionEvent.getSource()).getParentPopup().getOwnerWindow().getScene().getWindow());
            stage.showAndWait();

            // Only add assumption in case it was fully specified by the user.
            if (newAssumption.isFullySpecified()) {
                this.assumptions.getItems().add(newAssumption);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    private void saveToFile() {
        // Use default file if not otherwise set by the user.
        if(this.saveFile == null){
            this.saveFile = new File(this.defaultSaveLocation);
        }

        // Avoid overwriting in case a file with the default name already exists.
        if(this.saveFile.exists() && this.saveFile.getAbsolutePath().equals(this.defaultSaveLocation)){
            // Add number suffix until there is no conflict.
            int suffix = 1;
            do {
                this.saveFile = new File(this.defaultSaveLocation.substring(0, this.defaultSaveLocation.length() - 4) + suffix + ".asf");
                suffix++;
            } while (this.saveFile.exists());
        }

        // Write to save file.
    }

    @FXML
    private void saveAs(ActionEvent actionEvent) {
        var stage = (Stage) ((MenuItem) actionEvent.getSource()).getParentPopup().getOwnerWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Save Location");
        fileChooser.setInitialFileName("NewAssumptions.xml");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File chosenFile = fileChooser.showSaveDialog(stage);

        System.out.println(chosenFile);
    }

    @FXML
    private void handleQuit() {
        Platform.exit();
    }

    @FXML
    private void openDocumentation() {
        this.hostServices.showDocument("https://git.scc.kit.edu/i43/stud/praktika/sose2023/timnorbertbaechle");
    }

    @FXML
    private void handleAnalysisSelectionClick(MouseEvent event) {
        // Determine available analyses in case ComboBox is left-clicked and empty.
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            ComboBox<String> comboBox = (ComboBox<String>) event.getSource();
            if (comboBox.getItems().isEmpty()) {
                // Set available analyses.
                comboBox.setItems(this.getAvailableAnalyses());
            }
        }
    }

    @FXML
    private void handleAnalysisSelection(ActionEvent actionEvent) {
        ComboBox<String> comboBox = (ComboBox<String>) actionEvent.getSource();
        this.analysis = comboBox.getValue();
    }
}
