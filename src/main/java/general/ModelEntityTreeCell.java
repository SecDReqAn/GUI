package general;

import general.entities.ModelEntity;
import javafx.scene.control.TreeCell;

public class ModelEntityTreeCell extends TreeCell<ModelEntity> {
    private static final String MANUALLY_ANALYZED_STYLE_CLASS = "non-addable-tree-cell";
    @Override
    public void updateItem(ModelEntity modelEntity, boolean empty) {
        super.updateItem(modelEntity, empty);

        if (empty) {
            this.setText(null);
        } else {
            this.setText(modelEntity.getElementName().substring(0, 1).toUpperCase() + modelEntity.getElementName().substring(1) + "    "
                    + "Type: " + (modelEntity.getType() == null ? "N/A" : modelEntity.getType()) + "    "
                    + "Name: " + (modelEntity.getName() == null ? "N/A" : modelEntity.getName()) + "    "
                    + "Id: " + (modelEntity.getId() == null ? "N/A" : modelEntity.getId()));
            if(modelEntity.getId() == null || modelEntity.getId().isEmpty()){
                if(!this.getStyleClass().contains(ModelEntityTreeCell.MANUALLY_ANALYZED_STYLE_CLASS)) {
                    this.getStyleClass().add(ModelEntityTreeCell.MANUALLY_ANALYZED_STYLE_CLASS);
                }
            } else {
                this.getStyleClass().removeIf(style -> style.equals(ModelEntityTreeCell.MANUALLY_ANALYZED_STYLE_CLASS));
            }
        }
    }
}
