package io.local;

import general.Utilities;
import general.entities.ModelEntity;
import javafx.scene.control.TreeItem;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Class providing access to the model entities contained within a PCM model.
 */
public class ModelReader {
    /**
     * Custom {@link Comparator} for {@link TreeItem}s containing {@link ModelEntity} instances.
     */
    public static class EntityComparator implements Comparator<TreeItem<ModelEntity>> {
        @Override
        public int compare(TreeItem<ModelEntity> first, TreeItem<ModelEntity> second) {
            ModelEntity firstEntity = first.getValue();
            ModelEntity secondEntity = second.getValue();

            int typeComparison = Utilities.compareStrings(firstEntity.getType(), secondEntity.getType());
            if (typeComparison != 0) {
                return typeComparison;
            } else {
                int nameComparison = Utilities.compareStrings(firstEntity.getName(), secondEntity.getName());

                if (nameComparison != 0) {
                    return nameComparison;
                } else {
                    int idComparison = Utilities.compareStrings(firstEntity.getId(), secondEntity.getId());

                    if (idComparison != 0) {
                        return idComparison;
                    } else {
                        // ElementName is always set (i.e., != null).
                        return Utilities.compareStrings(firstEntity.getElementName(), secondEntity.getElementName());
                    }
                }
            }
        }
    }

    /**
     * A {@link Map} linking the model view {@link File}s contained in the model to their read root {@link TreeItem}.
     */
    private final Map<File, TreeItem<ModelEntity>> modelFileParsedItemMap;

    /**
     * Initializes the {@link ModelReader} instance with a model folder.
     * <p>
     * Note: This does not include the parsing of the model files.
     * </p>
     *
     * @param modelFolder The {@link File} describing the root folder of the model (i.e., the one containing the view-files).
     */
    public ModelReader(@NotNull File modelFolder) {
        this.modelFileParsedItemMap = new HashMap<>();

        var modelFiles = modelFolder.listFiles();
        if (modelFiles != null && modelFiles.length != 0) {
            Stream.of(modelFiles)
                    .filter(File::isFile)
                    .filter(file -> !file.getName().startsWith(".")) // Do not consider hidden files.
                    .forEach(file -> this.modelFileParsedItemMap.put(file, null));
        }
    }

    /**
     * Searches for a {@link TreeItem} containing a {@link ModelEntity} with a specified id by using depth first search (DFS) starting from a given root node.
     *
     * @param root The {@link TreeItem} acting as the starting point for the DFS.
     * @param id   The id of the {@link ModelEntity} in the desired {@link TreeItem}.
     * @return An {@link Optional} containing the found {@link TreeItem} if the search was successful. Otherwise, returns {@link Optional#empty()}.
     */
    private @NotNull Optional<TreeItem<ModelEntity>> searchTreeItemByElementId(@NotNull TreeItem<ModelEntity> root, @NotNull String id) {
        String rootId = root.getValue().getId();
        if (rootId != null && rootId.equals(id)) {
            return Optional.of(root);
        }

        for (var child : root.getChildren()) {
            if (this.searchTreeItemByElementId(child, id).isPresent()) {
                return Optional.of(child);
            }
        }

        return Optional.empty();
    }

    private void tryToResolveHref(Attribute href, TreeItem<ModelEntity> encompassingTreeItem) {
        String[] hrefComponents = href.getValue().split("#");
        if (hrefComponents.length == 2) {
            String hrefFile = hrefComponents[0];
            String hrefId = hrefComponents[1];

            // Check whether href can be resolved by searching in one of the model files.
            var referencedModelFile = this.modelFileParsedItemMap.keySet().stream().filter(file -> file.getName().equals(hrefFile)).findFirst();
            if (referencedModelFile.isPresent()) {
                Optional<TreeItem<ModelEntity>> rootOfReferencedModelFile = this.readFromModelFile(referencedModelFile.get());
                if (rootOfReferencedModelFile.isPresent()) {
                    var referencedTreeItem = this.searchTreeItemByElementId(rootOfReferencedModelFile.get(), hrefId);

                    if (referencedTreeItem.isPresent() && referencedTreeItem.get().getValue().hasOwnName()) {
                        StringBuilder newNameOfEntity = (encompassingTreeItem.getValue().getName() == null) ? new StringBuilder() : new StringBuilder(encompassingTreeItem.getValue().getName());

                        if (newNameOfEntity.isEmpty()) {
                            newNameOfEntity = new StringBuilder("Entity with hrefs to: ");
                        }

                        String nameOfReferencedEntity = referencedTreeItem.get().getValue().getName();
                        newNameOfEntity.append(hrefFile).
                                append("→").
                                append(nameOfReferencedEntity)
                                .append(" ");

                        encompassingTreeItem.getValue().setName(newNameOfEntity.toString().trim());
                    }
                }
            }


        }
    }

    /**
     * Returns the potential view-files contained in the model directory.
     *
     * @return A {@link Collection} containing the valid files found within the model directory.
     */
    public @NotNull Collection<File> getModelFiles() {
        return this.modelFileParsedItemMap.keySet();
    }

    /**
     * Reads the specified {@link File} (if it is a valid model file) and builds the contained tree structure.
     *
     * @param modelFile The {@link File} that should be read.
     * @return An {@link Optional} containing the root {@link TreeItem} read from the file or {@link Optional#empty()} if the specified file was either empty or not contained in the collection of valid model files, obtained via {@link ModelReader#getModelFiles()}.
     * @implNote Requires more memory this way (loading all entities at once) but is faster (and less complex) than trying to reparse specific segments of the XML-file on-demand.
     */
    public @NotNull Optional<TreeItem<ModelEntity>> readFromModelFile(@NotNull File modelFile) {
        // Only allow reading from a file within the specified model folder.
        if (!this.modelFileParsedItemMap.containsKey(modelFile)) {
            return Optional.empty();
        }

        // Check whether input file has already been read.
        if (this.modelFileParsedItemMap.get(modelFile) != null) {
            return Optional.of(this.modelFileParsedItemMap.get(modelFile));
        }

        TreeItem<ModelEntity> currentlySurroundingItem = null;
        try (var fileInputStream = new FileInputStream(modelFile)) {
            var inputFactory = XMLInputFactory.newInstance();
            var eventReader = inputFactory.createXMLEventReader(fileInputStream);

            while (eventReader.hasNext()) {
                XMLEvent nextEvent = eventReader.nextEvent();
                if (nextEvent.isStartElement()) {
                    StartElement startElement = nextEvent.asStartElement();
                    Attribute type = startElement.getAttributeByName(new QName(startElement.getNamespaceContext().getNamespaceURI("xsi"), "type", "xsi"));
                    Attribute id = startElement.getAttributeByName(new QName("id"));
                    Attribute name = startElement.getAttributeByName(new QName("entityName"));

                    var newTreeItem = new TreeItem<>(
                            new ModelEntity(
                                    id == null ? null : id.getValue(),
                                    modelFile.getName(), // Each view is specified by its own file.
                                    startElement.getName().getLocalPart(),
                                    name == null ? null : name.getValue(),
                                    type == null ? null : type.getValue()));

                    Attribute href = startElement.getAttributeByName(new QName("href"));
                    if (href != null && !newTreeItem.getValue().hasOwnName()) {
                        this.tryToResolveHref(href, newTreeItem);
                    }

                    if (currentlySurroundingItem != null) {
                        currentlySurroundingItem.getChildren().add(newTreeItem);
                    }
                    currentlySurroundingItem = newTreeItem;
                } else if (nextEvent.isEndElement() && currentlySurroundingItem != null) {
                    currentlySurroundingItem.getChildren().sort(new ModelReader.EntityComparator());

                    if (currentlySurroundingItem.getParent() != null) {
                        currentlySurroundingItem = currentlySurroundingItem.getParent(); // Traverse up.
                    }
                }
            }

        } catch (XMLStreamException | IOException ignored) {
        }

        this.modelFileParsedItemMap.put(modelFile, currentlySurroundingItem);
        return currentlySurroundingItem == null ? Optional.empty() : Optional.of(currentlySurroundingItem);
    }
}
