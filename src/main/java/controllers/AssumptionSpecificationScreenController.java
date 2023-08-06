package controllers;

import general.Assumption;
import general.ModelEntity;
import general.Utilities;
import io.ModelReader;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

public class AssumptionSpecificationScreenController {
    /**
     * The {@link Assumption} that is being specified.
     */
    private Assumption assumption;
    private ModelReader modelReader;

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
    private Button insertButton;
    @FXML
    private TreeView<ModelEntity> modelEntityTreeView;

    public void initAssumption(Assumption assumption) {
        this.assumption = assumption;

        // Set analyzed to false (default) if not already set.
        if (this.assumption.isAnalyzed() == null) {
            this.assumption.setAnalyzed(false);
        }

        // Initialize UI with possibly pre-existing data.
        this.initializeUIElements();
    }

    public void initModelFolder(String modelPath) {
        this.modelReader = new ModelReader(new File(modelPath));

        // Init ComboBox with available entities read from the selected model.
        this.modelViewComboBox.setItems(FXCollections.observableArrayList(this.modelReader.getModelFiles().stream().map(File::getName).sorted().toList()));
    }

    private void checkForCompletenessOfSpecification() {
        this.insertButton.setDisable(!this.assumption.isSufficientlySpecified());
    }

    private void initializeUIElements() {
        this.nameTextField.setText(this.assumption.getName() != null ? this.assumption.getName() : "");
        this.descriptionTextArea.setText(this.assumption.getDescription() != null ? this.assumption.getDescription() : "");
        this.riskTextField.setText(this.assumption.getRisk() != null ? String.valueOf(this.assumption.getRisk()) : "");
        this.violationProbabilityTextField.setText(this.assumption.getProbabilityOfViolation() != null ? String.valueOf(this.assumption.getProbabilityOfViolation()) : "");
        this.impactTextArea.setText(this.assumption.getImpact() != null ? this.assumption.getImpact() : "");
        this.riskTextField.setText(this.assumption.getRisk() != null ? String.valueOf(this.assumption.getRisk()) : "");

        if (this.assumption.getType() != null) {
            if (this.assumption.getType() == Assumption.AssumptionType.INTRODUCE_UNCERTAINTY) {
                this.introduceUncertaintyToggle.setSelected(true);
            } else {
                this.resolveUncertaintyToggle.setSelected(true);
            }
        }

        if (this.assumption.isAnalyzed() != null) {
            this.analyzedCheckBox.setSelected(this.assumption.isAnalyzed());
        }

        this.affectedEntityTableView.setItems(FXCollections.observableArrayList(this.assumption.getAffectedEntities()));
    }

    private void addContextMenus() {
        // Context menu for adding model entities from the TreeView.
        Utilities.addFunctionalityToContextMenu(this.modelEntityTreeView, "Add to Affected Model Entities", (ActionEvent actionEvent) -> {
            ModelEntity selectedModelEntity = this.modelEntityTreeView.getSelectionModel().getSelectedItem().getValue();

            if (selectedModelEntity != null && this.assumption.getAffectedEntities().add(selectedModelEntity)) {
                this.affectedEntityTableView.getItems().add(selectedModelEntity);
                this.checkForCompletenessOfSpecification();
            }
        });

        // Context menu for removing affected model entity within the table view.
        Utilities.addFunctionalityToContextMenu(this.affectedEntityTableView, "Remove Model Entity", (ActionEvent actionEvent) -> {
            ModelEntity selectedModelEntity = this.affectedEntityTableView.getSelectionModel().getSelectedItem();

            if (selectedModelEntity != null && this.assumption.getAffectedEntities().remove(selectedModelEntity)) {
                this.affectedEntityTableView.getItems().remove(selectedModelEntity);
            }
        });
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

        this.affectedEntityNameColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getName()));
        this.affectedEntityTypeColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getType()));
        this.affectedEntityIdColumn.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getId()));

        this.addContextMenus();
    }

    @FXML
    private void handleAnalyzedToggle(ActionEvent actionEvent) {
        var checkBox = (CheckBox) actionEvent.getSource();
        this.assumption.setAnalyzed(checkBox.isSelected());
        this.checkForCompletenessOfSpecification();
    }

    @FXML
    private void handleModelViewSelection() {
        String selectedModelViewName = this.modelViewComboBox.getValue();
        Optional<File> selectedModelViewFile = this.modelReader.getModelFiles().stream().filter(file -> file.getName().equals(selectedModelViewName)).findAny();

        if (selectedModelViewFile.isPresent()) {
            Optional<TreeItem<ModelEntity>> readTreeItem = this.modelReader.readFromModelFile(selectedModelViewFile.get());

            if(readTreeItem.isPresent()){
                this.modelEntityTreeView.setRoot(readTreeItem.get());
                this.modelEntityTreeView.refresh();
            }
        }
    }

    @FXML
    private void handleInsertButton(ActionEvent actionEvent) {
        var stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }
}
