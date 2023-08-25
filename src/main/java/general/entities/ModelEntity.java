package general.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModelEntity {
    @Nullable private String id;
    @NotNull private String modelView;
    @NotNull private String elementName;
    @Nullable private String name;
    @Nullable private String type;
    @JsonIgnore
    private boolean ownName;

    /**
     * Default constructor as <b>required</b> for Jackson deserialization.
     */
    @SuppressWarnings("unused")
    private ModelEntity() {
    }

    public ModelEntity(@Nullable String id, @NotNull String modelView, @NotNull String elementName, @Nullable String name, @Nullable String type) {
        this.id = id;
        this.modelView = modelView;
        this.elementName = elementName;
        this.name = name;
        this.type = type;
        this.ownName = this.name != null;
    }

    public @Nullable String getId() {
        return this.id;
    }

    public @NotNull String getModelView() {
        return this.modelView;
    }

    public @NotNull String getElementName() {
        return this.elementName;
    }

    public @Nullable String getName() {
        return this.name;
    }

    public @Nullable String getType() {
        return this.type;
    }

    public boolean hasOwnName() {
        return this.ownName;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }
}
