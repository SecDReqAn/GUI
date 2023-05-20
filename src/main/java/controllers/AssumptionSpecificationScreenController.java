package controllers;

import general.Assumption;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;

public class AssumptionSpecificationScreenController {
    /**
     * The {@link Assumption} that is being specified.
     */
    private Assumption assumption;

    @FXML
    private TextArea descriptionTextArea;

    public AssumptionSpecificationScreenController(){
        this.assumption = new Assumption();
    }

    @FXML
    public void initialize(){
        // Listen for changes of the text in the description TextArea.
        this.descriptionTextArea.textProperty().addListener((obs, oldText, newText) -> {
            this.assumption.setDescription(newText);
        });
    }

    @FXML
    public void handleAnalyzedToggle(ActionEvent actionEvent){
        var checkBox = (CheckBox) actionEvent.getSource();
        this.assumption.setAnalyzed(checkBox.isSelected());
    }
}
