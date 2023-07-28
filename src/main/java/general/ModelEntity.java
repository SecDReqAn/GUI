package general;

import org.jetbrains.annotations.NotNull;

public record ModelEntity(@NotNull String id, @NotNull String modelView, String elementName, String name,
                          String type) {
    @Override
    public String toString() {
        return (this.name == null ? "" : ("Name: " + this.name + " ")) +
                (this.type == null ? "" : ("Type: " + this.type + " ")) +
                "Id: " + this.id;
    }

}
