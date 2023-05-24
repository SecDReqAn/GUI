package controllers;

import general.Assumption;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class MainScreenController {
    private String analysis = "";

    private ArrayList<Assumption> assumptions = new ArrayList<>();
    @FXML
    public void handleNewAssumption(ActionEvent actionEvent) {
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
            stage.setTitle("New Assumption Specification");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((MenuItem) actionEvent.getSource()).getParentPopup().getOwnerWindow().getScene().getWindow());
            stage.showAndWait();

            // Only add assumption in case it was fully specified by the user.
            if(newAssumption.isFullySpecified()){
                this.assumptions.add(newAssumption);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // TODO Delete
        for(var assumption : this.assumptions){
            System.out.println(assumption);
        }
    }

    @FXML
    public void handleAnalysisSelectionClick(MouseEvent event) {
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
    public void handleAnalysisSelection(ActionEvent actionEvent) {
        ComboBox<String> comboBox = (ComboBox<String>) actionEvent.getSource();
        this.analysis = comboBox.getValue();
    }

    private ObservableList<String> getAvailableAnalyses() {
        // TODO Determine the available analyses.
        return FXCollections.observableArrayList("Abunai", "Analysis 2", "Analysis 3");
    }
}
