package general.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class representing a model entity found within a PCM.
 */
public class ModelEntity {
    /**
     * The unique id of the entity within the PCM.
     */
    @Nullable
    private String id;
    /**
     * The view in which the entity was specified.
     */
    @NotNull
    private String modelView;
    /**
     * The name of the XML element that declared the entity.
     */
    @NotNull
    private String elementName;
    /**
     * The name of the entity.
     */
    @Nullable
    private String name;
    /**
     * The type of the entity.
     */
    @Nullable
    private String type;
    /**
     * A property indicating whether the definition of this entity (read from one of the PCM files) contained a name
     * specification.
     */
    @JsonIgnore
    private boolean ownName;

    /**
     * Default constructor as <b>required</b> for Jackson deserialization.
     */
    @SuppressWarnings("unused")
    private ModelEntity() {
    }

    public ModelEntity(@Nullable String id, @NotNull String modelView, @NotNull String elementName,
                       @Nullable String name, @Nullable String type) {
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
