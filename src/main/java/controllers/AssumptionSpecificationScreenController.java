package controllers;

import general.ModelEntityTreeCell;
import general.Utilities;
import general.entities.GraphAssumption;
import general.entities.AssumptionType;
import general.entities.ModelEntity;
import io.local.ModelReader;
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
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * The dedicated controller managing the assumption-specification screen that is initiated by
 * {@link MainScreenController}.
 */
public class AssumptionSpecificationScreenController {
    /**
     * The {@link GraphAssumption} that is being specified / edited.
     */
    private GraphAssumption assumption;
    /**
     * The {@link Collection} of already specified {@link GraphAssumption}s.
     */
    private Collection<GraphAssumption> specifiedAssumptions;
    /**
     * The {@link ModelReader} that is used for accessing model entities.
     */
    private ModelReader modelReader;
    /**
     * A flag indicating whether the user confirmed his input / changes.
     */
    private boolean userConfirmation;

    // FXML controls.
    @FXML
    private VBox topLevelVBox;
    @FXML
    private TextField nameTextField;
    @FXML
    private MenuButton dependenciesMenuButton;
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
     * Default constructor.
     */
    public AssumptionSpecificationScreenController() {
        this.userConfirmation = false;
    }

    /**
     * Initializes the controller with data required for the assumption specification process.
     *
     * @param specifiedAssumptions The {@link Collection} of {@link GraphAssumption}s that the user has already entered
     *                             into the application.
     * @param assumption           The {@link GraphAssumption} instance that should be filled with data by the user.
     * @param modelPath            The absolute path to the folder of the PCM model.
     */
    public void initWithMainData(@NotNull Collection<GraphAssumption> specifiedAssumptions,
                                 @NotNull GraphAssumption assumption,
                                 @NotNull String modelPath) {
        this.assumption = assumption;
        this.specifiedAssumptions = specifiedAssumptions;
        // Initialize UI with (existing) assumption data.
        this.initializeUIElements();

        this.modelReader = new ModelReader(new File(modelPath));
        // Init TableView with available entities read from the selected model.
        this.modelViewTableView.setItems(FXCollections.observableArrayList(this.modelReader.getModelFiles()));
    }

    /**
     * Gets the boolean indicating whether the user confirmed his input.
     *
     * @return The boolean, indicating whether the user confirmed his input (<code>true</code>) or whether
     * he aborted the specification (<code>false</code>).
     */
    public boolean getUserConfirmation() {
        return this.userConfirmation;
    }

    /**
     * Checks whether the mandatory fields of the {@link GraphAssumption} instance have been filled and enables a
     * button to exit the pop-up window.
     */
    private void checkForCompletenessOfSpecification() {
        this.insertButton.setDisable(!this.assumption.isSufficientlySpecified());
    }

    /**
     * Populates the GUI control elements with existing values in case
     * {@link AssumptionSpecificationScreenController#assumption} already contains data. This is the case if the user
     * decided to edit an already existing {@link GraphAssumption}.
     */
    private void initializeUIElements() {
        this.nameTextField.setText(this.assumption.getName() != null ? this.assumption.getName() : "");
        this.descriptionTextArea.setText(this.assumption.getDescription() != null ?
                this.assumption.getDescription() : "");
        this.riskTextField.setText(this.assumption.getRisk() != null ? String.valueOf(this.assumption.getRisk()) : "");
        this.violationProbabilityTextField.setText(this.assumption.getProbabilityOfViolation() != null ?
                String.valueOf(this.assumption.getProbabilityOfViolation()) : "");
        this.impactTextArea.setText(this.assumption.getImpact() != null ? this.assumption.getImpact() : "");
        this.riskTextField.setText(this.assumption.getRisk() != null ? String.valueOf(this.assumption.getRisk()) : "");

        if (this.assumption.getType() != null) {
            if (this.assumption.getType() == AssumptionType.INTRODUCE_UNCERTAINTY) {
                this.introduceUncertaintyToggle.setSelected(true);
            } else {
                this.resolveUncertaintyToggle.setSelected(true);
            }
        }

        this.analyzedCheckBox.setSelected(this.assumption.isAnalyzed());

        // Populate dependenciesMenuButton with content.
        this.specifiedAssumptions.forEach(specifiedAssumption -> {
            // Do not allow a dependency on itself.
            if (!this.assumption.getId().equals(specifiedAssumption.getId())) {
                var dependencyCheckMenuItem = new CheckMenuItem(specifiedAssumption.getName() +
                        " (Id: " + specifiedAssumption.getId().toString().substring(0, 5) + "...)");
                Collection<UUID> dependenciesOfCurrentAssumption = this.assumption.getDependencies();

                // Set CheckMenuItem to selected id dependency is already present.
                if (dependenciesOfCurrentAssumption.contains(specifiedAssumption.getId())) {
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
     * Convenience method that adds appropriate context menus to
     * {@link AssumptionSpecificationScreenController#modelEntityTreeView} and
     * {@link AssumptionSpecificationScreenController#affectedEntityTableView}.
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

                Utilities.addFunctionalityToContextMenu(this.modelEntityTreeView,
                                                        "Add to Affected Model Entities",
                                                        (ActionEvent actionEvent) -> {
                    TreeItem<ModelEntity> selectedItemDuringContextInitiation =
                            this.modelEntityTreeView.getSelectionModel().getSelectedItem();

                    if (selectedItemDuringContextInitiation != null
                            && this.assumption.getAffectedEntities().add(selectedItemDuringContextInitiation.getValue())) {
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
        Utilities.addFunctionalityToContextMenu(this.affectedEntityTableView,
                                                "Remove Model Entity",
                                                (ActionEvent actionEvent) -> {
            ModelEntity selectedModelEntity = this.affectedEntityTableView.getSelectionModel().getSelectedItem();

            if (selectedModelEntity != null && this.assumption.getAffectedEntities().remove(selectedModelEntity)) {
                this.affectedEntityTableView.getItems().remove(selectedModelEntity);
            }
        });
    }

    /**
     * Initializer function called on scene creation.
     */
    @FXML
    private void initialize() {
        this.topLevelVBox.setAlignment(Pos.CENTER);

        // Init user data for the toggle buttons.
        resolveUncertaintyToggle.setUserData(AssumptionType.RESOLVE_UNCERTAINTY);
        introduceUncertaintyToggle.setUserData(AssumptionType.INTRODUCE_UNCERTAINTY);

        // Listen for changes of the text in the name TextField.
        this.nameTextField.textProperty().addListener((observable, oldText, newText) -> {
            this.assumption.setName(newText.trim());
            this.checkForCompletenessOfSpecification();
        });

        // Listen for changes with regard to the toggle-group.
        this.typeToggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            this.assumption.setType((AssumptionType) newToggle.getUserData());
            this.checkForCompletenessOfSpecification();
        });

        // Listen for changes of the text in the description TextArea.
        this.descriptionTextArea.textProperty().addListener((observable, oldText, newText) -> {
            this.assumption.setDescription(newText.trim());
            this.checkForCompletenessOfSpecification();
        });

        // Listen for changes of the text in the probability of violation TextField.
        this.violationProbabilityTextField.textProperty().addListener((observable, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                return;
            }

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
            if (newText == null || newText.isEmpty()) {
                return;
            }

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
     * Handles a click on the "Analyzed" check box.
     *
     * @param actionEvent The {@link ActionEvent} triggered by the click.
     */
    @FXML
    private void handleAnalyzedToggle(ActionEvent actionEvent) {
        var checkBox = (CheckBox) actionEvent.getSource();
        this.assumption.setAnalyzed(checkBox.isSelected());
        this.checkForCompletenessOfSpecification();
    }

    /**
     * Handles a click on the "Insert" button at the bottom of the scene.
     *
     * @param actionEvent The {@link ActionEvent} triggered by the click.
     */
    @FXML
    private void handleInsertButton(ActionEvent actionEvent) {
        this.userConfirmation = true;

        var stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }
}
