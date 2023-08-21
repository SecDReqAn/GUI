package controllers;

import general.entities.Assumption;
import general.entities.ModelEntity;
import general.ModelEntityTreeCell;
import general.Utilities;
import io.ModelReader;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;
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
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The dedicated controller managing the assumption-specification screen that is initiated by {@link MainScreenController}.
 */
public class AssumptionSpecificationScreenController {
    /**
     * The {@link Assumption} that is being specified.
     */
    private Assumption assumption;
    /**
     * The {@link Collection} of already specified {@link Assumption}s.
     */
    private Collection<Assumption> specifiedAssumptions;
    /**
     * The {@link ModelReader} that is used for accessing model entities.
     */
    private ModelReader modelReader;

    @FXML
    private VBox topLevelVBox;
    @FXML
    private TextField nameTextField;
    @FXML
    private MenuButton dependenciesMenuButton; // TODO: Consider replacing with some kind of graph visualization (maybe https://github.com/jgrapht/jgrapht).
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
    private TableView<File> modelViewTableView;
    @FXML
    private TableColumn<File, String> modelViewTableColumn;
    @FXML
    private Button insertButton;
    @FXML
    private TreeView<ModelEntity> modelEntityTreeView;

    /**
     * Initializes the controller with data required for the assumption specification process.
     *
     * @param specifiedAssumptions The {@link Collection} of {@link Assumption}s that the user has already entered into the application.
     * @param assumption           The {@link Assumption} instance that should be filled with data by the user.
     * @param modelPath            The absolute path to the folder of the PCM model.
     */
    public void initWithMainData(Collection<Assumption> specifiedAssumptions, Assumption assumption, String modelPath) {
        this.assumption = assumption;
        this.specifiedAssumptions = specifiedAssumptions;

        // Set analyzed to false (default) if not already set.
        if (this.assumption.isAnalyzed() == null) {
            this.assumption.setAnalyzed(false);
        }

        // Initialize UI with (existing) assumption data.
        this.initializeUIElements();

        this.modelReader = new ModelReader(new File(modelPath));
        // Init TableView with available entities read from the selected model.
        this.modelViewTableView.setItems(FXCollections.observableArrayList(this.modelReader.getModelFiles()));
    }

    /**
     * Checks whether the mandatory fields of the {@link Assumption} instance have been filled and enables a button to exit the pop-up window.
     */
    private void checkForCompletenessOfSpecification() {
        this.insertButton.setDisable(!this.assumption.isSufficientlySpecified());
    }

    /**
     * Populates the GUI control elements with existing values in case {@link AssumptionSpecificationScreenController#assumption} already contains data. This is the case if the user decided to edit an already existing {@link Assumption}.
     */
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

        // Populate dependenciesMenuButton with content.
        this.specifiedAssumptions.forEach(specifiedAssumption -> {
            // Do not allow a dependency on itself.
            if (!this.assumption.getId().equals(specifiedAssumption.getId())) {
                var dependencyCheckMenuItem = new CheckMenuItem(specifiedAssumption.getName() + " (Id: " + specifiedAssumption.getId().toString().substring(0, 5) + "...)");
                Set<UUID> dependenciesOfCurrentAssumption = this.assumption.getDependencies();

                // Set CheckMenuItem to selected id dependency is already present.
                if(dependenciesOfCurrentAssumption.contains(specifiedAssumption.getId())){
                    dependencyCheckMenuItem.setSelected(true);
                }

                // Deal with (de)selection of the CheckMenuItem
                dependencyCheckMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        // Add new dependency.
                        dependenciesOfCurrentAssumption.add(specifiedAssumption.getId());
                    } else {
                        // (Potentially) remove existing dependency.
                        dependenciesOfCurrentAssumption.remove(specifiedAssumption.getId());
                    }
                });

                this.dependenciesMenuButton.getItems().add(dependencyCheckMenuItem);
            }
        });
        if (this.dependenciesMenuButton.getItems().isEmpty()) {
            this.dependenciesMenuButton.setDisable(true);
        }

        this.affectedEntityTableView.setItems(FXCollections.observableArrayList(this.assumption.getAffectedEntities()));
    }

    /**
     * Convenience method that adds appropriate context menus to {@link AssumptionSpecificationScreenController#modelEntityTreeView} and {@link AssumptionSpecificationScreenController#affectedEntityTableView}.
     */
    private void addContextMenus() {
        // Show context menu for the TreeView only for the ModelEntities that are associated with an id.
        this.modelEntityTreeView.getSelectionModel().selectedItemProperty().addListener(event -> {
            TreeItem<ModelEntity> selectedItem = this.modelEntityTreeView.getSelectionModel().getSelectedItem();

            if (selectedItem != null && selectedItem.getValue().getId() != null) {
                // Nothing to do if ContextMenu is already set.
                if (this.modelEntityTreeView.getContextMenu() != null) {
                    return;
                }

                Utilities.addFunctionalityToContextMenu(this.modelEntityTreeView, "Add to Affected Model Entities", (ActionEvent actionEvent) -> {
                    TreeItem<ModelEntity> selectedItemDuringContextInitiation = this.modelEntityTreeView.getSelectionModel().getSelectedItem();

                    if (selectedItemDuringContextInitiation != null && this.assumption.getAffectedEntities().add(selectedItemDuringContextInitiation.getValue())) {
                        this.affectedEntityTableView.getItems().add(selectedItemDuringContextInitiation.getValue());
                        this.affectedEntityTableView.scrollTo(this.affectedEntityTableView.getItems().size() - 1);
                        this.checkForCompletenessOfSpecification();
                    }
                });
            } else {
                // Do not show the context menu for invalid items.
                this.modelEntityTreeView.setContextMenu(null);
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

    /**
     * Initializes various GUI control elements on creation of the scene.
     */
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
                this.violationProbabilityTextField.getStyleClass().removeIf(style -> style.equals("text-input-error"));
            } catch (NullPointerException | NumberFormatException exception) {
                if (!this.violationProbabilityTextField.getStyleClass().contains("text-input-error")) {
                    this.violationProbabilityTextField.getStyleClass().add("text-input-error");
                }
            }

            this.checkForCompletenessOfSpecification();
        });

        // Listen for changes of the text in the risk TextField.
        this.riskTextField.textProperty().addListener((observable, oldText, newText) -> {
            try {
                this.assumption.setRisk(Double.parseDouble(newText));

                // Clear potential red error border.
                this.riskTextField.getStyleClass().removeIf(style -> style.equals("text-input-error"));
            } catch (NullPointerException | NumberFormatException exception) {
                if (!this.riskTextField.getStyleClass().contains("text-input-error")) {
                    this.riskTextField.getStyleClass().add("text-input-error");
                }
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

        this.modelViewTableColumn.setCellValueFactory(cellData -> {
            File file = cellData.getValue();
            return new ReadOnlyStringWrapper(file == null ? "" : file.getName());
        });

        // Handle selection of one of the PCM model's views.
        this.modelViewTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldFile, newFile) -> {
            Optional<TreeItem<ModelEntity>> readTreeItem = this.modelReader.readFromModelFile(newFile);

            this.modelEntityTreeView.setRoot(readTreeItem.orElse(null));
            this.modelEntityTreeView.refresh();
        });

        this.modelEntityTreeView.setCellFactory((TreeView<ModelEntity> p) -> new ModelEntityTreeCell());

        this.addContextMenus();
    }

    /**
     * Handles a press on the "Analyzed" check box.
     *
     * @param actionEvent The {@link ActionEvent} triggered through the press.
     */
    @FXML
    private void handleAnalyzedToggle(ActionEvent actionEvent) {
        var checkBox = (CheckBox) actionEvent.getSource();
        this.assumption.setAnalyzed(checkBox.isSelected());
        this.checkForCompletenessOfSpecification();
    }

    /**
     * Handles a press on the "Insert" button at the bottom of the scene.
     *
     * @param actionEvent The triggered {@link ActionEvent} through the press.
     */
    @FXML
    private void handleInsertButton(ActionEvent actionEvent) {
        var stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }
}
