package general;

import general.entities.ModelEntity;
import javafx.scene.control.TreeCell;

/**
 * Custom {@link TreeCell} for {@link ModelEntity}s that sets the displayed text and grays itself out if the entity
 * does not have an id (i.e., it is not addable to the affected entities set).
 */
public class ModelEntityTreeCell extends TreeCell<ModelEntity> {
    private static final String MANUALLY_ANALYZED_STYLE_CLASS = "non-addable-tree-cell";

    @Override
    public void updateItem(ModelEntity modelEntity, boolean empty) {
        super.updateItem(modelEntity, empty);

        if (empty) {
            this.setText(null);
        } else {
            this.setText(
                    modelEntity.getElementName().substring(0, 1).toUpperCase() + modelEntity.getElementName().substring(
                            1) + "    "
                            + "Type: " + (modelEntity.getType() == null ? "N/A" : modelEntity.getType()) + "    "
                            + "Name: " + (modelEntity.getName() == null ? "N/A" : modelEntity.getName()) + "    "
                            + "Id: " + (modelEntity.getId() == null ? "N/A" : modelEntity.getId()));

            // Gray out non-addable entities.
            if (modelEntity.getId() == null || modelEntity.getId().isEmpty()) {
                if (!this.getStyleClass().contains(ModelEntityTreeCell.MANUALLY_ANALYZED_STYLE_CLASS)) {
                    this.getStyleClass().add(ModelEntityTreeCell.MANUALLY_ANALYZED_STYLE_CLASS);
                }
            } else {
                this.getStyleClass().removeIf(style -> style.equals(ModelEntityTreeCell.MANUALLY_ANALYZED_STYLE_CLASS));
            }
        }
    }
}
