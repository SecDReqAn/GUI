package controllers;

import general.Assumption;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AssumptionSpecificationScreenController {
    /**
     * The {@link Assumption} that is being specified.
     */
    private final Assumption assumption;

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

    public AssumptionSpecificationScreenController() {
        this.assumption = new Assumption();
    }

    @FXML
    public void initialize() {
        // Init user data for the toggle buttons.
        resolveUncertaintyToggle.setUserData(Assumption.AssumptionType.RESOLVE_UNCERTAINTY);
        introduceUncertaintyToggle.setUserData(Assumption.AssumptionType.INTRODUCE_UNCERTAINTY);

        // Listen for changes with regard to the toggle-group.
        this.typeToggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            this.assumption.setType((Assumption.AssumptionType) newToggle.getUserData());
        });

        // Listen for changes of the text in the description TextArea.
        this.descriptionTextArea.textProperty().addListener((observable, oldText, newText) -> {
            this.assumption.setDescription(newText);
        });

        // Listen for changes of the text in the probability of violation TextField.
        this.violationProbabilityTextField.textProperty().addListener((observable, oldText, newText) -> {
            // TODO Error handling.
            this.assumption.setProbabilityOfViolation(Double.parseDouble(newText));
        });

        // Listen for changes of the text in the risk TextField.
        this.riskTextField.textProperty().addListener((observable, oldText, newText) -> {
            // TODO Error handling.
            this.assumption.setRisk(Double.parseDouble(newText));
        });

        // Listen for changes of the text in the impact TextArea.
        this.impactTextArea.textProperty().addListener((observable, oldText, newText) -> {
            this.assumption.setImpact(newText);
        });
    }

    @FXML
    public void handleAnalyzedToggle(ActionEvent actionEvent) {
        var checkBox = (CheckBox) actionEvent.getSource();
        this.assumption.setAnalyzed(checkBox.isSelected());
    }

    @FXML
    public void handleInsertButton(){
        // TODO Transfer assumption back to main screen.
        System.out.println(this.assumption);
    }
}
