package controllers;

import general.Assumption;
import io.ModelReader;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Map;

public class AssumptionSpecificationScreenController {
    /**
     * The {@link Assumption} that is being specified.
     */
    private Assumption assumption;
    private Map<String, Map<String, ModelReader.ModelEntity>> modelEntityMap;

    @FXML
    private VBox topLevelVBox;
    @FXML
    private ToggleGroup typeToggleGroup;
    @FXML
    private ToggleButton resolveUncertaintyToggle;
    @FXML
    private ToggleButton introduceUncertaintyToggle;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private TextField violationProbabilityTextField;
    @FXML
    private TextField riskTextField;
    @FXML
    private TextArea impactTextArea;
    @FXML
    private ComboBox<String> modelViewComboBox;
    @FXML
    private ComboBox<ModelReader.ModelEntity> modelEntityComboBox;
    @FXML
    private Button insertButton;

    public void initAssumption(Assumption assumption) {
        this.assumption = assumption;

        // Analyzed is defaulted to false.
        this.assumption.setAnalyzed(false);
    }

    public void initModelEntities(Map<String, Map<String, ModelReader.ModelEntity>> modelEntityMap) {
        this.modelEntityMap = modelEntityMap;

        // Init ComboBox with available entities read from the selected model.
        this.modelViewComboBox.setItems(FXCollections.observableArrayList(this.modelEntityMap.keySet()).sorted());
    }

    private void checkForCompletenessOfSpecification() {
        this.insertButton.setDisable(!this.assumption.isFullySpecified());
    }

    @FXML
    private void initialize() {
        this.topLevelVBox.setAlignment(Pos.CENTER);

        // Init user data for the toggle buttons.
        resolveUncertaintyToggle.setUserData(Assumption.AssumptionType.RESOLVE_UNCERTAINTY);
        introduceUncertaintyToggle.setUserData(Assumption.AssumptionType.INTRODUCE_UNCERTAINTY);

        // Listen for changes with regard to the toggle-group.
        this.typeToggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            this.assumption.setType((Assumption.AssumptionType) newToggle.getUserData());
            this.checkForCompletenessOfSpecification();
        });

        // Listen for changes of the text in the description TextArea.
        this.descriptionTextArea.textProperty().addListener((observable, oldText, newText) -> {
            this.assumption.setDescription(newText.trim());
            this.checkForCompletenessOfSpecification();
        });

        // Listen for changes of the text in the probability of violation TextField.
        this.violationProbabilityTextField.textProperty().addListener((observable, oldText, newText) -> {
            try {
                this.assumption.setProbabilityOfViolation(Double.parseDouble(newText));

                // Clear potential red error border.
                this.violationProbabilityTextField.setStyle(null);
                this.violationProbabilityTextField.setStyle("-fx-padding: 5pt");
            } catch (NullPointerException | NumberFormatException exception) {
                // Invalidate probability of violation field in assumption.
                this.assumption.setProbabilityOfViolation(null);
                this.violationProbabilityTextField.setStyle(this.violationProbabilityTextField.getStyle() + "; -fx-text-box-border: red; -fx-focus-color: red ;");
            }

            this.checkForCompletenessOfSpecification();
        });

        // Listen for changes of the text in the risk TextField.
        this.riskTextField.textProperty().addListener((observable, oldText, newText) -> {
            try {
                this.assumption.setRisk(Double.parseDouble(newText));

                // Clear potential red error border.
                this.riskTextField.setStyle(null);
                this.riskTextField.setStyle("-fx-padding: 5pt");
            } catch (NullPointerException | NumberFormatException exception) {
                // Invalidate risk field in assumption.
                this.assumption.setRisk(null);
                this.riskTextField.setStyle(this.riskTextField.getStyle() + "; -fx-text-box-border: red; -fx-focus-color: red ;");
            }

            this.checkForCompletenessOfSpecification();
        });

        // Listen for changes of the text in the impact TextArea.
        this.impactTextArea.textProperty().addListener((observable, oldText, newText) -> {
            this.assumption.setImpact(newText.trim());
            this.checkForCompletenessOfSpecification();
        });
    }

    @FXML
    private void handleAnalyzedToggle(ActionEvent actionEvent) {
        var checkBox = (CheckBox) actionEvent.getSource();
        this.assumption.setAnalyzed(checkBox.isSelected());
        this.checkForCompletenessOfSpecification();
    }

    @FXML
    private void handleModelViewSelection() {
        var selectedModelView = this.modelViewComboBox.getValue();
        this.modelEntityComboBox.setItems(
                FXCollections.observableArrayList(
                        this.modelEntityMap.get(selectedModelView).values()).sorted(new ModelReader.EntityComparator()));
        this.modelEntityComboBox.setDisable(false);
    }

    @FXML
    private void handleAffectedEntitySelection() {
        ModelReader.ModelEntity associatedModelEntity = this.modelEntityComboBox.getValue();
        this.assumption.setAffectedEntity(associatedModelEntity.id());
        this.checkForCompletenessOfSpecification();
    }

    @FXML
    private void handleInsertButton(ActionEvent actionEvent) {
        var stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }
}
