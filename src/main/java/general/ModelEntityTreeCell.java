package general;

import javafx.scene.control.TreeCell;

public class ModelEntityTreeCell extends TreeCell<ModelEntity> {
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
                if(!this.getStyleClass().contains("non-addable-tree-cell")) {
                    this.getStyleClass().add("non-addable-tree-cell");
                }
            } else {
                this.getStyleClass().removeIf(style -> style.equals("non-addable-tree-cell"));
            }
        }
    }
}
