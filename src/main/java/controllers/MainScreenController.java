package controllers;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import general.Constants;
import general.Utilities;
import general.entities.AnalysisResult;
import general.entities.Assumption;
import general.entities.Configuration;
import general.entities.SecurityCheckAssumption;
import io.AnalysisConnector;
import io.ConfigManager;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// TODO: Make use of @NotNull and @Nullable annotations to avoid NullPointer-Exceptions.

/**
 * The dedicated controller managing the main screen that is entered on start-up of the application.
 */
public class MainScreenController {
    /**
     * A {@link String} specifying the default path for the save-file.
     */
    private final String defaultSaveLocation = Constants.USER_HOME_PATH + Constants.FILE_SYSTEM_SEPARATOR + Constants.DEFAULT_SAVE_FILE_NAME;
    /**
     * The {@link Configuration} that is being edited by the user.
     */
    private Configuration currentConfiguration;
    /**
     * The last {@link Configuration}-state that is persisted on disk.
     */
    private Configuration savedConfiguration;
    /**
     * Connector allowing for communication with a (potentially) remote analysis microservice.
     */
    private AnalysisConnector analysisConnector;
    /**
     * Service that periodically checks the connection to the analysis.
     */
    private final ScheduledExecutorService connectionTestService;
    /**
     * Services allowing access to e.g. the default browser of the host system.
     */
    private HostServices hostServices;
    /**
     * The actual {@link File} to which changes are persisted upon a save-operation.
     */
    private File saveFile;

    @FXML
    private Button performAnalysisButton;
    @FXML
    private TableView<Assumption> assumptionTableView;
    @FXML
    private TableColumn<Assumption, UUID> idColumn;
    @FXML
    private TableColumn<Assumption, String> nameColumn;
    @FXML
    private TableColumn<Assumption, Assumption.AssumptionType> typeColumn;
    @FXML
    private TableColumn<Assumption, String> descriptionColumn;
    @FXML
    private TableColumn<Assumption, String> entitiesColumn;
    @FXML
    private TableColumn<Assumption, String> dependenciesColumn;
    @FXML
    private TableColumn<Assumption, Double> violationProbabilityColumn;
    @FXML
    private TableColumn<Assumption, Double> riskColumn;
    @FXML
    private TableColumn<Assumption, String> impactColumn;
    @FXML
    private TableColumn<Assumption, Boolean> analyzedColumn;
    @FXML
    private TextArea analysisOutputTextArea;
    @FXML
    private TableView<AnalysisResult> analysisOutputTableView;
    @FXML
    private TableColumn<AnalysisResult, String> outputTitleColumn;
    @FXML
    private Label modelNameLabel;
    @FXML
    private Label analysisPathLabel;
    @FXML
    private Label connectionStatusLabel;
    @FXML
    private Label statusLabel;

    /**
     * Constructs a new instance and initializes the associated {@link Configuration}s.
     */
    public MainScreenController() {
        this.savedConfiguration = new Configuration();
        this.currentConfiguration = new Configuration();

        this.connectionTestService = Executors.newSingleThreadScheduledExecutor();
        connectionTestService.scheduleAtFixedRate(() -> {
            if (MainScreenController.this.analysisConnector != null) {
                var connectionTestCode = MainScreenController.this.analysisConnector.testConnection().getKey();
                Platform.runLater(() -> MainScreenController.this.connectionStatusLabel.setText(
                        connectionTestCode == 200 ? Constants.CONNECTION_SUCCESS_TEXT : Constants.CONNECTION_FAILURE_TEXT));
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * Sets {@link MainScreenController#hostServices} to the specified instance.
     *
     * @param hostServices The {@link HostServices} that should be set.
     */
    public void setHostServices(@NotNull HostServices hostServices) {
        this.hostServices = hostServices;
    }

    /**
     * Checks for unsaved changes in the current configuration and alerts the user if necessary.
     *
     * @param alertContent The {@link String} that should be shown as part of the {@link Alert} pop-up in case of unsaved changes.
     */
    public void alertUserOfUnsavedChanges(@NotNull String alertContent) {
        if (this.hasUnsavedChanges()) {
            var confirmationResult = Utilities.showAlert(Alert.AlertType.CONFIRMATION,
                    "Unsaved Changes",
                    "There exist unsaved changed in the current configuration",
                    alertContent,
                    new ButtonType("Save Changes", ButtonBar.ButtonData.YES),
                    new ButtonType("Discard Changes", ButtonBar.ButtonData.NO));

            if (confirmationResult.isPresent() && confirmationResult.get().getButtonData() == ButtonBar.ButtonData.YES) {
                this.saveToFile();
            }
        }
    }

    /**
     * Handles an exit request by the user, initiated by a click on the X button of the window.
     */
    public void handleExitRequest() {
        this.alertUserOfUnsavedChanges("Save changes before exiting?");
        this.connectionTestService.shutdown();
    }

    /**
     * Open modal window for the assumption specification-screen.
     *
     * @param assumption The {@link Assumption} instance that can be edited through the assumption specification-screen.
     * @param owner      The {@link Window} owning the modal window to be created.
     */
    private void showAssumptionSpecificationScreen(@NotNull Assumption assumption, @NotNull Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("../UI/AssumptionSpecificationScreen.fxml"));
            AnchorPane root = loader.load();

            AssumptionSpecificationScreenController controller = loader.getController();
            controller.initWithMainData(this.currentConfiguration.getAssumptions(), assumption, this.currentConfiguration.getModelPath());

            var stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Assumption Specification");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(owner);
            stage.showAndWait();

        } catch (IOException e) {
            // TODO Proper handling of this case.
            throw new RuntimeException(e);

        }
    }

    /**
     * Checks whether the current configuration ({@link MainScreenController#currentConfiguration}) contains unsaved changes (i.e., changes not reflected in {@link MainScreenController#savedConfiguration}).
     *
     * @return <code>true</code> if there are unsaved changes and <code>false</code> otherwise.
     */
    private boolean hasUnsavedChanges() {
        return !this.currentConfiguration.equals(this.savedConfiguration);
    }

    private void clearControlElements() {
        this.assumptionTableView.getItems().clear();
        this.analysisOutputTextArea.setText("");
        this.analysisOutputTableView.getItems().clear();
        this.modelNameLabel.setText("No model folder selected");
        this.analysisPathLabel.setText("No analysis URI specified");
        this.connectionStatusLabel.setText(Constants.CONNECTION_FAILURE_TEXT);
        this.statusLabel.setText("");
        this.performAnalysisButton.setDisable(true);
    }

    private void initializeAssumptionTableView(){
        // Extract fields of an assumption into their appropriate column of the TableView.
        this.idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        this.nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        this.typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        this.descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        this.dependenciesColumn.setCellValueFactory(new PropertyValueFactory<>("dependencies"));
        this.violationProbabilityColumn.setCellValueFactory(new PropertyValueFactory<>("probabilityOfViolation"));
        this.riskColumn.setCellValueFactory(new PropertyValueFactory<>("risk"));
        this.impactColumn.setCellValueFactory(new PropertyValueFactory<>("impact"));
        this.analyzedColumn.setCellValueFactory(new PropertyValueFactory<>("analyzed"));

        // Deal with columns that contain collections.
        Utilities.setCellValueFactoryForCollectionElement(this.entitiesColumn, assumption -> {
            var stringBuilder = new StringBuilder();
            assumption.getAffectedEntities().forEach(affectedEntity -> {
                stringBuilder.append(affectedEntity.getId());
                stringBuilder.append(", ");
            });
            return stringBuilder.isEmpty() ? "" : stringBuilder.substring(0, stringBuilder.length() - 2);
        });
        Utilities.setCellValueFactoryForCollectionElement(this.dependenciesColumn, assumption -> {
            var stringBuilder = new StringBuilder();
            assumption.getDependencies().forEach(dependency -> {
                Optional<Assumption> associatedAssumption = this.currentConfiguration.getAssumptions().stream()
                        .filter(assumptionInConfig -> assumptionInConfig.getId().equals(dependency)).findFirst();

                if (associatedAssumption.isPresent()) {
                    stringBuilder.append("\"").append(associatedAssumption.get().getName()).append("\"");
                    stringBuilder.append(" (Id: ");
                    stringBuilder.append(dependency.toString(), 0, Math.min(5, dependency.toString().length()));
                    stringBuilder.append("...),\n");
                }
            });
            return stringBuilder.isEmpty() ? "" : stringBuilder.substring(0, stringBuilder.length() - 2);
        });

        // Enable text-warp in text-centric columns.
        Utilities.enableTextWrapForTableColumn(this.descriptionColumn);
        Utilities.enableTextWrapForTableColumn(this.impactColumn);
        Utilities.enableTextWrapForTableColumn(this.entitiesColumn);
        Utilities.enableTextWrapForTableColumn(this.dependenciesColumn);

        // Anonymous helper function used in the ContextMenu and on double click.
        Runnable retrieveAssumptionAndOpenSpecificationScreen = () -> {
            Assumption selectedAssumption = this.assumptionTableView.getSelectionModel().getSelectedItem();

            if (selectedAssumption != null) {
                this.showAssumptionSpecificationScreen(selectedAssumption, this.assumptionTableView.getScene().getWindow());
                this.assumptionTableView.refresh();
            }
        };

        // Context menu item for editing assumptions within the table view.
        Utilities.addFunctionalityToContextMenu(this.assumptionTableView, "Edit Assumption", (ActionEvent actionEvent) -> retrieveAssumptionAndOpenSpecificationScreen.run());
        // Context menu item for marking an assumption as "manually analyzed".
        Utilities.addFunctionalityToContextMenu(this.assumptionTableView, "Toggle manually analyzed", (ActionEvent actionEvent) -> {
            Assumption selectedAssumption = this.assumptionTableView.getSelectionModel().getSelectedItem();
            selectedAssumption.setManuallyAnalyzed(!selectedAssumption.getManuallyAnalyzed());
        });
        Utilities.addSeparatorToContextMenu(this.assumptionTableView);

        // Context menu item for removing an assumptions from the table view.
        Utilities.addFunctionalityToContextMenu(this.assumptionTableView, "Remove Assumption", (ActionEvent actionEvent) -> {
            Assumption assumptionForDeletion = this.assumptionTableView.getSelectionModel().getSelectedItem();

            if (assumptionForDeletion != null && this.currentConfiguration.getAssumptions().remove(assumptionForDeletion)) {
                // Remove deleted assumption from dependency lists of other assumptions if necessary.
                this.currentConfiguration.getAssumptions().forEach(specifiedAssumption -> assumptionForDeletion.getDependencies().remove(assumptionForDeletion.getId()));

                this.assumptionTableView.getItems().remove(assumptionForDeletion);
                this.assumptionTableView.refresh();
            }
        });

        // Custom RowFactory for double-click functionality and custom styling based on "manuallyAnalyzed" property.
        this.assumptionTableView.setRowFactory(tv -> {
            var row = new TableRow<Assumption>() {
                @Override
                protected void updateItem(Assumption assumption, boolean empty){
                    super.updateItem(assumption, empty);

                    if(assumption != null && assumption.getManuallyAnalyzed()){
                        // Enable custom styling and disable even / odd styling.
                        if(!this.getStyleClass().contains("manually-analyzed-row")) {
                            this.getStyleClass().add("manually-analyzed-row");
                        }
                        this.pseudoClassStateChanged(PseudoClass.getPseudoClass(this.getIndex() % 2 == 0 ? "even" : "odd"), false);
                    } else {
                        // Disable custom styling and turn even / odd styling back on.
                        this.getStyleClass().removeIf(styleClass -> styleClass.equals("manually-analyzed-row"));
                        this.pseudoClassStateChanged(PseudoClass.getPseudoClass(this.getIndex() % 2 == 0 ? "even" : "odd"), true);
                    }
                }
            };

            row.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() == 2 && (!row.isEmpty())) {
                    retrieveAssumptionAndOpenSpecificationScreen.run();
                }
            });
            return row;
        });
    }

    private void initializeAnalysisOutputTableView(){
        this.outputTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        this.analysisOutputTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.analysisOutputTextArea.setText(newValue.getResult());
            }
        });

        // Context menu for removing an existing analysis result.
        Utilities.addFunctionalityToContextMenu(this.analysisOutputTableView, "Remove Analysis Output", (ActionEvent actionEvent) -> {
            AnalysisResult analysisResultForDeletion = this.analysisOutputTableView.getSelectionModel().getSelectedItem();

            if (analysisResultForDeletion != null && this.currentConfiguration.getAnalysisResults().remove(analysisResultForDeletion)) {
                this.analysisOutputTableView.getItems().remove(analysisResultForDeletion);

                // Deal with TextArea if necessary.
                if (this.analysisOutputTableView.getSelectionModel().getSelectedItem() == null) {
                    this.analysisOutputTextArea.setText("No outputs found");
                }

            }
        });
    }

    @FXML
    private void initialize() {
        this.initializeAssumptionTableView();
        this.initializeAnalysisOutputTableView();

        // Allow the title of an analysis result ot be renamed by the user.
        this.outputTitleColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        this.outputTitleColumn.setOnEditCommit(
                (TableColumn.CellEditEvent<AnalysisResult, String> t) -> {
                    String desiredTitle = t.getNewValue();
                    AnalysisResult selectedAnalysisResult = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    // Check that the desired title is not already present.
                    Optional<AnalysisResult> conflictingAnalysisResult = this.currentConfiguration.getAnalysisResults().stream()
                            .filter(analysisResult -> analysisResult.getTitle().equals(desiredTitle)).findFirst();

                    if(conflictingAnalysisResult.isEmpty()) {
                        selectedAnalysisResult.setTitle(desiredTitle);
                    } else {
                        if(conflictingAnalysisResult.get() != selectedAnalysisResult) {
                            Utilities.showAlert(Alert.AlertType.WARNING,
                                    "Invalid Title",
                                    "The entered title \"" + desiredTitle + "\" is already used for another output!",
                                    "The title will be reset to its prior value.");
                            this.analysisOutputTableView.refresh();
                        }
                    }
                });

        // analysisPathLabel and connectionStatusLabel should show the same effect once one is hovered over.
        this.analysisPathLabel.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !this.connectionStatusLabel.getStyleClass().contains("label-hover")) {
                this.connectionStatusLabel.getStyleClass().add("label-hover");
            } else {
                this.connectionStatusLabel.getStyleClass().removeIf(styleClass -> styleClass.equals("label-hover"));
            }
        });
        this.connectionStatusLabel.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !this.analysisPathLabel.getStyleClass().contains("label-hover")) {
                this.analysisPathLabel.getStyleClass().add("label-hover");
            } else {
                this.analysisPathLabel.getStyleClass().removeIf(styleClass -> styleClass.equals("label-hover"));
            }
        });
    }

    @FXML
    private void handleNewAssumption(ActionEvent actionEvent) {
        if (this.currentConfiguration.getModelPath() == null || !(new File(this.currentConfiguration.getModelPath()).exists())) {
            Utilities.showAlert(Alert.AlertType.WARNING,
                    "Warning",
                    "Unable to create a new assumption!",
                    "A path to a valid model has to be set.");
            return;
        }

        // Create new modal window for entry of assumption parameters.
        Assumption newAssumption = new Assumption();
        this.showAssumptionSpecificationScreen(newAssumption, ((MenuItem) actionEvent.getSource()).getParentPopup().getOwnerWindow());

        // Only add assumption in case it was fully specified by the user.
        if (newAssumption.isSufficientlySpecified()) {
            this.currentConfiguration.getAssumptions().add(newAssumption);
            this.assumptionTableView.getItems().add(newAssumption);
            this.performAnalysisButton.setDisable(this.currentConfiguration.isMissingAnalysisParameters());
        }
    }

    @FXML
    private void handleNewConfiguration(ActionEvent actionEvent) {
        var stage = Utilities.getStageOfMenuItem((MenuItem) actionEvent.getSource());

        this.alertUserOfUnsavedChanges("Save changes before opening a new configuration?");
        stage.setTitle(Constants.DEFAULT_STAGE_TITLE);
        this.currentConfiguration = new Configuration();
        this.savedConfiguration = new Configuration();
        this.analysisConnector = null;
        this.saveFile = null;

        this.clearControlElements();
    }

    @FXML
    private void openFromFile(@NotNull ActionEvent actionEvent) {
        var stage = Utilities.getStageOfMenuItem((MenuItem) actionEvent.getSource());

        var fileChooser = new FileChooser();
        fileChooser.setTitle("Select an existing File");
        fileChooser.setInitialFileName("NewAssumptions.xml");
        fileChooser.setInitialDirectory(new File(Constants.USER_HOME_PATH));
        var selectedFile = fileChooser.showOpenDialog(stage);

        // File selection has been aborted by the user.
        if (selectedFile == null) {
            return;
        }

        if (!selectedFile.exists() || !selectedFile.isFile()) {
            Utilities.showAlert(Alert.AlertType.WARNING, "Warning", "Loading Failed", "The specified file could not be read!");
        }

        this.alertUserOfUnsavedChanges("Save changes before opening file?");
        this.clearControlElements();

        try {
            this.savedConfiguration = ConfigManager.readConfig(selectedFile);
            this.currentConfiguration = this.savedConfiguration.clone();

            // Analysis path and connector
            this.analysisPathLabel.setText(this.currentConfiguration.getAnalysisPath());
            this.analysisConnector = new AnalysisConnector(this.currentConfiguration.getAnalysisPath());

            // Model
            if (this.currentConfiguration.getModelPath() != null) {
                var folders = this.currentConfiguration.getModelPath().split(Constants.FILE_SYSTEM_SEPARATOR.equals("\\") ? "\\\\" : Constants.FILE_SYSTEM_SEPARATOR);
                this.modelNameLabel.setText(folders[folders.length - 1]);
            }

            // Assumptions
            this.assumptionTableView.getItems().clear();
            this.assumptionTableView.getItems().addAll(this.currentConfiguration.getAssumptions());

            // Analysis result
            this.analysisOutputTableView.getItems().addAll(this.currentConfiguration.getAnalysisResults());
            if (!this.analysisOutputTableView.getItems().isEmpty()) {
                AnalysisResult firstAnalysisResultInTable = this.analysisOutputTableView.getItems().get(0);
                this.analysisOutputTableView.getSelectionModel().select(0);
                this.analysisOutputTextArea.setText(firstAnalysisResultInTable.getResult());
            }

            this.saveFile = selectedFile;
            this.performAnalysisButton.setDisable(this.currentConfiguration.isMissingAnalysisParameters());
            stage.setTitle(Constants.APPLICATION_NAME + " — " + this.saveFile.getName());
        } catch (StreamReadException e) {
            Utilities.showAlert(Alert.AlertType.ERROR, "Error", "Opening file failed", "The specified file exhibits an invalid structure.");
        } catch (DatabindException e) {
            Utilities.showAlert(Alert.AlertType.ERROR, "Error", "Opening file failed", "Could not map the contents of the specified file to a valid Configuration.");
        } catch (FileNotFoundException e) {
            Utilities.showAlert(Alert.AlertType.ERROR, "Error", "Opening file failed", "Could not find the repository file associated with the the specified model.");
        } catch (IOException e) {
            Utilities.showAlert(Alert.AlertType.ERROR, "Error", "Opening file failed", "Encountered a low-level I/O problem when trying to read from the file.");
        }
    }

    @FXML
    private void openRecent() {
        // TODO Maybe start with recent files in the current execution. Otherwise some form of persistent config file is required.
        System.out.println("Not implemented!");
    }

    @FXML
    private void saveToFile() {
        if (this.currentConfiguration.isEmpty()) {
            return;
        }

        // Use default file if not otherwise set by the user.
        if (this.saveFile == null) {
            this.saveFile = new File(this.defaultSaveLocation);
        }

        // Avoid overwriting in case a file with the default name already exists.
        if (this.saveFile.exists() && this.saveFile.getAbsolutePath().equals(this.defaultSaveLocation)) {
            // Add number suffix until there is no conflict.
            int suffix = 1;
            do {
                this.saveFile = new File(this.defaultSaveLocation.substring(0, this.defaultSaveLocation.length() - 4) + suffix + ".xml");
                suffix++;
            } while (this.saveFile.exists());
        }

        // Write to save-file.
        try {
            this.saveFile.createNewFile();
            ConfigManager.writeConfig(this.saveFile, this.currentConfiguration);
            this.savedConfiguration = this.currentConfiguration;

            Stage stage = (Stage) this.assumptionTableView.getScene().getWindow();
            stage.setTitle(Constants.APPLICATION_NAME + " — " + this.saveFile.getName());
        } catch (IOException e) {
            Utilities.showAlert(Alert.AlertType.ERROR, "Error", "Saving failed", "Could not write to file!");
        }
    }

    @FXML
    private void saveAs(@NotNull ActionEvent actionEvent) {
        var stage = Utilities.getStageOfMenuItem((MenuItem) actionEvent.getSource());

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Save Location");
        fileChooser.setInitialFileName("NewAssumptions.json");
        fileChooser.setInitialDirectory(new File(Constants.USER_HOME_PATH));
        this.saveFile = fileChooser.showSaveDialog(stage);

        this.saveToFile();
    }

    @FXML
    private void handleQuit() {
        if (!this.savedConfiguration.equals(this.currentConfiguration)) {
            var confirmationResult = Utilities.showAlert(Alert.AlertType.CONFIRMATION,
                    "Unsaved Changes",
                    "There exist unsaved changed in the current configuration",
                    "Quit and discard the changes?");

            if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.CANCEL) {
                return;
            }
        }

        this.connectionTestService.shutdown();
        Platform.exit();
    }

    @FXML
    private void openDocumentation() {
        this.hostServices.showDocument(Constants.DOCUMENTATION_URL);
    }

    @FXML
    private void handleAnalysisExecution() {
        // Check whether forwarding the request to the analysis makes sense.
        if (!this.currentConfiguration.isMissingAnalysisParameters()) {
            this.statusLabel.setText("Trying to connect to analysis...");

            // Execute manual connection test to analysis.
            if (this.analysisConnector != null && this.analysisConnector.testConnection().getKey() == 200) {
                this.statusLabel.setText("Transmit selected model to the analysis...");
                Pair<Integer, String> modelTransferResponse = this.analysisConnector.transferModelFiles(
                        new File(Objects.requireNonNull(this.currentConfiguration.getModelPath())));
                if (modelTransferResponse.getKey() != 200) {
                    this.statusLabel.setText("Analysis aborted.");
                    Utilities.showAlert(Alert.AlertType.ERROR, "Error", "Transmission of the selected model to the analysis failed.", modelTransferResponse.getValue());
                    return;
                }

                this.statusLabel.setText("Starting analysis...");
                Pair<Integer, AnalysisConnector.AnalysisOutput> analysisResponse = this.analysisConnector.performAnalysis(this.currentConfiguration.getModelPath(), this.currentConfiguration.getAssumptions());
                String analysisResponseLog = analysisResponse.getValue().outputLog();
                Collection<SecurityCheckAssumption> analysisResponseAssumptions = analysisResponse.getValue().assumptions();

                if (analysisResponse.getKey() != 0) {
                    this.statusLabel.setText("Analysis successfully executed.");

                    var analysisResultTitle = new SimpleDateFormat("dd.MM.yy. HH:mm:ss").format(new Date());
                    var newAnalysisResult = new AnalysisResult(analysisResultTitle, analysisResponseLog);

                    this.currentConfiguration.getAnalysisResults().add(newAnalysisResult);
                    this.analysisOutputTableView.getItems().add(newAnalysisResult);
                    this.analysisOutputTableView.getSelectionModel().select(newAnalysisResult);
                    this.analysisOutputTextArea.setText(newAnalysisResult.getResult());


                    // Update assumptions.
                    if (analysisResponseAssumptions != null) {
                        this.assumptionTableView.getItems().clear();

                        analysisResponseAssumptions.forEach(securityCheckAssumption -> {
                            this.currentConfiguration.getAssumptions().stream()
                                    .filter(assumption -> assumption.getId().equals(securityCheckAssumption.id()))
                                    .findFirst().ifPresent(matchingAssumption -> matchingAssumption.updateWith(securityCheckAssumption));
                        });
                        this.assumptionTableView.setItems(FXCollections.observableArrayList(this.currentConfiguration.getAssumptions()));
                        this.assumptionTableView.refresh();
                    }
                } else {
                    this.statusLabel.setText("Received invalid response from the analysis.");
                    Utilities.showAlert(Alert.AlertType.ERROR, "Error", "Communication with the analysis failed.", analysisResponseLog);
                }
            } else {
                this.statusLabel.setText("Analysis aborted.");
                Utilities.showAlert(Alert.AlertType.ERROR, "Error", "Communication with the analysis failed.", "Connection to the analysis could not be established.");
            }
        } else {
            // TODO Error
            System.out.println("Error (Not implemented)");
        }
    }

    @FXML
    private void handleAnalysisPathSelection() {
        var textInputDialog = new TextInputDialog(this.currentConfiguration.getAnalysisPath() == null ? Constants.DEFAULT_ANALYSIS_PATH : this.currentConfiguration.getAnalysisPath());
        textInputDialog.setTitle("Analysis URI");
        textInputDialog.setHeaderText("Please provide the web-service URI of the analysis.");
        textInputDialog.setContentText("URI:");

        var userInput = textInputDialog.showAndWait();

        userInput.ifPresent(input -> {
            this.currentConfiguration.setAnalysisPath(input);
            this.analysisPathLabel.setText(input);
            this.analysisConnector = new AnalysisConnector(input);
            this.performAnalysisButton.setDisable(this.currentConfiguration.isMissingAnalysisParameters());
        });
    }

    @FXML
    private void handleModelNameSelection(MouseEvent mouseEvent) {
        var originatingLabel = (Label) mouseEvent.getSource();
        var stage = (Stage) originatingLabel.getScene().getWindow();

        var directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Model Folder");
        directoryChooser.setInitialDirectory(new File(Constants.USER_HOME_PATH));
        var selectedFolder = directoryChooser.showDialog(stage);

        // Check whether the user aborted the selection.
        if (selectedFolder != null) {
            var absolutePathToSelectedFolder = selectedFolder.getAbsolutePath();

            // Check whether the specified folder actually contains a repository file.
            File modelFolder = new File(absolutePathToSelectedFolder);
            if (modelFolder.exists()) {
                // Accept valid selection.
                this.currentConfiguration.setModelPath(absolutePathToSelectedFolder);
                // Only display last subfolder of the path for better readability.
                var folders = Objects.requireNonNull(this.currentConfiguration.getModelPath()).
                        split(Constants.FILE_SYSTEM_SEPARATOR.equals("\\") ? "\\\\" : Constants.FILE_SYSTEM_SEPARATOR);
                originatingLabel.setText(folders[folders.length - 1]);

                this.performAnalysisButton.setDisable(this.currentConfiguration.isMissingAnalysisParameters());
            } else {
                // Invalid selection due to missing repository file.
                Utilities.showAlert(Alert.AlertType.ERROR, "Error", "Loading model entities failed", "The repository file (default.repository) of the specified model could not be found.");
            }
        }
    }
}
