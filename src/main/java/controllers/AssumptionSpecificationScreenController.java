package controllers;

import general.Assumption;
import general.ModelEntity;
import io.ModelReader;
import javafx.beans.property.SimpleStringProperty;
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
    private Map<String, Map<String, ModelEntity>> modelEntityMap;

    @FXML
    private VBox topLevelVBox;
    @FXML
    private TextField nameTextField;
    @FXML
    private ToggleGroup typeToggleGroup;
    @FXML
    private ToggleButton resolveUncertaintyToggle;
    @FXML
    private ToggleButton introduceUncertaintyToggle;
    @FXML
    private CheckBox analyzedCheckBox;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private TextField violationProbabilityTextField;
    @FXML
    private TextField riskTextField;
    @FXML
    private TextArea impactTextArea;
    @FXML
    private TableView<ModelEntity> affectedEntityTableView;
    @FXML
    private TableColumn<ModelEntity, String> affectedEntityNameColumn;
    @FXML
    private TableColumn<ModelEntity, String> affectedEntityTypeColumn;
    @FXML
    private TableColumn<ModelEntity, String> affectedEntityIdColumn;
    @FXML
    private ComboBox<String> modelViewComboBox;
    @FXML
    private ComboBox<ModelEntity> modelEntityComboBox;
    @FXML
    private Button insertButton;

    public void initAssumption(Assumption assumption) {
        this.assumption = assumption;

        // Set analyzed to false (default) if not already set.
        if(this.assumption.isAnalyzed() == null){
            this.assumption.setAnalyzed(false);
        }

        // Initialize UI with possibly pre-existing data.
        this.initUI();
    }

    public void initModelEntities(Map<String, Map<String, ModelEntity>> modelEntityMap) {
        this.modelEntityMap = modelEntityMap;

        // Init ComboBox with available entities read from the selected model.
        this.modelViewComboBox.setItems(FXCollections.observableArrayList(this.modelEntityMap.keySet()).sorted());
    }

    private void checkForCompletenessOfSpecification() {
        this.insertButton.setDisable(!this.assumption.isSufficientlySpecified());
    }

    private void initUI(){
        this.nameTextField.setText(this.assumption.getName() != null ? this.assumption.getName() : "");
        this.descriptionTextArea.setText(this.assumption.getDescription() != null ? this.assumption.getDescription() : "");
        this.riskTextField.setText(this.assumption.getRisk() != null ? String.valueOf(this.assumption.getRisk()) : "");
        this.violationProbabilityTextField.setText(this.assumption.getProbabilityOfViolation() != null ? String.valueOf(this.assumption.getProbabilityOfViolation()) : "");
        this.impactTextArea.setText(this.assumption.getImpact() != null ? this.assumption.getImpact() : "");
        this.riskTextField.setText(this.assumption.getRisk() != null ? String.valueOf(this.assumption.getRisk()) : "");

        if(this.assumption.getType() != null){
            if(this.assumption.getType() == Assumption.AssumptionType.INTRODUCE_UNCERTAINTY){
                this.introduceUncertaintyToggle.setSelected(true);
            } else {
                this.resolveUncertaintyToggle.setSelected(true);
            }
        }

        if(this.assumption.isAnalyzed() != null){
            this.analyzedCheckBox.setSelected(this.assumption.isAnalyzed());
        }

        this.affectedEntityTableView.setItems(FXCollections.observableArrayList(this.assumption.getAffectedEntities()));
    }

    @FXML
    private void initialize() {
        this.topLevelVBox.setAlignment(Pos.CENTER);

        // Init user data for the toggle buttons.
        resolveUncertaintyToggle.setUserData(Assumption.AssumptionType.RESOLVE_UNCERTAINTY);
        introduceUncertaintyToggle.setUserData(Assumption.AssumptionType.INTRODUCE_UNCERTAINTY);

        // Listen for changes of the text in the name TextField.
        this.nameTextField.textProperty().addListener((observable, oldText, newText) -> {
            this.assumption.setName(newText.trim());
            this.checkForCompletenessOfSpecification();
        });

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

        this.affectedEntityNameColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().name()));
        this.affectedEntityTypeColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().type()));
        this.affectedEntityIdColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().id()));
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
        // Is also called when items property is changed via other ComboBox.
        if (this.modelEntityComboBox.getValue() != null) {
            ModelEntity selectedModelEntity = this.modelEntityComboBox.getValue();
            if (this.assumption.getAffectedEntities().add(selectedModelEntity)) {
                this.affectedEntityTableView.getItems().add(selectedModelEntity);
            }
            this.checkForCompletenessOfSpecification();
        }
    }

    @FXML
    private void handleInsertButton(ActionEvent actionEvent) {
        var stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }
}
