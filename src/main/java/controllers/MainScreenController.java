package controllers;

import general.Assumption;
import general.Configuration;
import general.ModelEntity;
import general.Utilities;
import io.ConfigManager;
import io.ModelReader;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import network.AnalysisConnector;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainScreenController {
    private static final String COMPONENT_REPOSITORY_FILENAME = "default.repository";
    private final String defaultSaveLocation;
    private AnalysisConnector analysisConnector;
    private HostServices hostServices;
    private File saveFile;
    private boolean isSaved;
    private String analysisPath;
    private String modelPath;

    private Map<String, ModelEntity> modelEntityMap;

    @FXML
    private ListView<Assumption> assumptions;
    @FXML
    private Label analysisPathLabel;
    @FXML
    private Label modelNameLabel;

    public MainScreenController() {
        this.defaultSaveLocation = System.getProperty("user.home") + System.getProperty("file.separator") + "NewAssumptionSet.xml";
        this.isSaved = true;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    private void handleNewAssumption(ActionEvent actionEvent) {
        if (this.analysisPath == null || this.analysisPath.isEmpty() ||
                this.modelEntityMap == null || this.modelEntityMap.isEmpty()) {
            Utilities.showAlertPopUp(Alert.AlertType.WARNING, "Warning", "Unable to create a new assumption!", "A path to a valid model and analysis first has to be set.");
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
            controller.initModelEntities(this.modelEntityMap);

            var stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Assumption Specification");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((MenuItem) actionEvent.getSource()).getParentPopup().getOwnerWindow().getScene().getWindow());
            stage.showAndWait();

            // Only add assumption in case it was fully specified by the user.
            if (newAssumption.isFullySpecified()) {
                this.assumptions.getItems().add(newAssumption);
                this.isSaved = false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    private void newAnalysis() {
        // TODO Leverage isSavedField.
        System.out.println("Not implemented!");
    }

    @FXML
    private void openFromFile(ActionEvent actionEvent) {
        var stage = (Stage) ((MenuItem) actionEvent.getSource()).getParentPopup().getOwnerWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an existing File");
        fileChooser.setInitialFileName("NewAssumptions.xml");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        var selectedFile = fileChooser.showOpenDialog(stage);

        // File selection has been aborted by the user.
        if (selectedFile == null) {
            return;
        }

        if (!selectedFile.exists() || !selectedFile.isFile()) {
            var alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Loading Failed");
            alert.setContentText("The specified file could not be read!");

            alert.show();
        }

        try {
            Configuration configuration = ConfigManager.readConfig(selectedFile);

            // Init model entities from read model path.
            this.modelEntityMap = ModelReader.readFromRepositoryFile(new File(configuration.getModelPath()
                    + System.getProperty("file.separator")
                    + MainScreenController.COMPONENT_REPOSITORY_FILENAME));

            this.analysisPath = configuration.getAnalysisPath();
            this.analysisPathLabel.setText(this.analysisPath);

            this.modelPath = configuration.getModelPath();
            var folders = this.modelPath.split(System.getProperty("file.separator"));
            this.modelNameLabel.setText(folders[folders.length - 1]);

            this.assumptions.getItems().clear();
            this.assumptions.getItems().addAll(configuration.getAssumptions());

            this.saveFile = selectedFile;
            this.isSaved = true;
        } catch (XMLStreamException e) {
            Utilities.showAlertPopUp(Alert.AlertType.ERROR, "Error", "Opening file failed", "The specified file is not a well-formed configuration.");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            Utilities.showAlertPopUp(Alert.AlertType.ERROR, "Error", "Opening file failed", "The specified file could not be found.");
            e.printStackTrace();
        }
    }

    @FXML
    private void openRecent() {
        // TODO Maybe start with recent files in the current execution. Otherwise some form of persistent config file is required.
        System.out.println("Not implemented!");
    }

    @FXML
    private void saveToFile() {
        // No save file necessary if configuration is empty.
        if (this.assumptions.getItems().isEmpty() && this.analysisPath == null && this.modelPath == null) {
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
        Set<Assumption> assumptions = new HashSet<>(this.assumptions.getItems());
        try {
            this.saveFile.createNewFile();
            ConfigManager.writeConfig(this.saveFile, new Configuration(this.analysisPath, this.modelPath, assumptions));
            this.isSaved = true;
        } catch (FileNotFoundException e) {
            Utilities.showAlertPopUp(Alert.AlertType.ERROR, "Error", "Saving failed", "The specified save location could not be found!");
            e.printStackTrace();
        } catch (IOException e) {
            Utilities.showAlertPopUp(Alert.AlertType.ERROR, "Error", "Saving failed", "Could not write to file!");
            e.printStackTrace();
        } catch (XMLStreamException e) {
            Utilities.showAlertPopUp(Alert.AlertType.ERROR, "Error", "Saving failed", "The current configuration is not well formed!");
            e.printStackTrace();
        }
    }

    @FXML
    private void saveAs(ActionEvent actionEvent) {
        var stage = (Stage) ((MenuItem) actionEvent.getSource()).getParentPopup().getOwnerWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Save Location");
        fileChooser.setInitialFileName("NewAssumptions.xml");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        this.saveFile = fileChooser.showSaveDialog(stage);

        this.saveToFile();
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
    private void handleAnalysisPathSelection(MouseEvent mouseEvent) {
        var originatingLabel = (Label) mouseEvent.getSource();

        var textInputDialog = new TextInputDialog();
        textInputDialog.setTitle("Analysis URI");
        textInputDialog.setHeaderText("Please provide the web-service URI of the analysis.");
        textInputDialog.setContentText("URI:");

        var userInput = textInputDialog.showAndWait();

        if (userInput.isPresent()) {
            this.analysisConnector = new AnalysisConnector(userInput.get());

            // Test connection to analysis.
            var connectionTestResult = this.analysisConnector.testConnection();
            if (connectionTestResult.getKey() == 200) {
                this.analysisPath = userInput.get();
                this.isSaved = false;
                originatingLabel.setText(this.analysisPath + " ✓");
            } else {
                Utilities.showAlertPopUp(Alert.AlertType.ERROR, "Error", "Could not connect to the analysis", connectionTestResult.getValue());
            }
        }
    }

    @FXML
    private void handleModelNameSelection(MouseEvent mouseEvent) {
        var originatingLabel = (Label) mouseEvent.getSource();
        var stage = (Stage) originatingLabel.getScene().getWindow();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Model Folder");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        var selectedFolder = directoryChooser.showDialog(stage);

        // Check whether the user aborted the selection.
        if (selectedFolder != null) {
            var absolutePath = selectedFolder.getAbsolutePath();

            // Check whether the specified folder actually contains a repository file.
            File repositoryFile = new File(absolutePath + System.getProperty("file.separator") + MainScreenController.COMPONENT_REPOSITORY_FILENAME);
            if (repositoryFile.exists()) {
                // Load contents of repository file.
                try {
                    this.modelEntityMap = ModelReader.readFromRepositoryFile(repositoryFile);

                    // Accept valid selection.
                    this.modelPath = absolutePath;
                    var folders = this.modelPath.split(System.getProperty("file.separator"));
                    originatingLabel.setText(folders[folders.length - 1]);
                    this.isSaved = false;
                } catch (FileNotFoundException e) {
                    Utilities.showAlertPopUp(Alert.AlertType.ERROR, "Error", "Loading model entities failed", "The repository file (default.repository) of the specified model could not be found.");
                    e.printStackTrace();
                } catch (XMLStreamException e) {
                    Utilities.showAlertPopUp(Alert.AlertType.ERROR, "Error", "Loading model entities failed", "The repository file (default.repository) of the specified model was not well-formed.");
                    e.printStackTrace();
                }
            } else {
                // Invalid selection due to missing repository file.
                Utilities.showAlertPopUp(Alert.AlertType.ERROR, "Error", "Loading model entities failed", "The repository file (default.repository) of the specified model could not be found.");
            }
        }
    }
}
