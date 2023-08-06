package io;

import general.ModelEntity;
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

public class ModelReader {
    public static class EntityComparator implements Comparator<TreeItem<ModelEntity>> {
        @Override
        public int compare(TreeItem<ModelEntity> first, TreeItem<ModelEntity> second) {
            ModelEntity firstEntity = first.getValue();
            ModelEntity secondEntity = second.getValue();
            if (firstEntity.getType() != null && secondEntity.getType() == null) {
                return -1;
            } else if (firstEntity.getType() == null && secondEntity.getType() != null) {
                return 1;
            } else if (firstEntity.getType() != null) {
                return firstEntity.getType().compareTo(secondEntity.getType());
            } else {
                if (firstEntity.getName() != null && secondEntity.getName() == null) {
                    return -1;
                } else if (firstEntity.getName() == null && secondEntity.getName() != null) {
                    return 1;
                } else if (firstEntity.getName() != null) {
                    return firstEntity.getName().compareTo(secondEntity.getName());
                } else {
                    return firstEntity.getId().compareTo(secondEntity.getId());
                }
            }
        }
    }

    private final Map<File, TreeItem<ModelEntity>> modelFileParsedItemMap;

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

    private @NotNull Optional<TreeItem<ModelEntity>> searchTreeItemByElementId(@NotNull TreeItem<ModelEntity> root, @NotNull String id) {
        if (root.getValue().getId().equals(id)) {
            return Optional.of(root);
        }

        for (var child : root.getChildren()) {
            if (this.searchTreeItemByElementId(child, id).isPresent()) {
                return Optional.of(child);
            }
        }

        return Optional.empty();
    }

    private void tryToResolveHref(StartElement currentStartElement, Attribute href, TreeItem<ModelEntity> currentlySurroundingItem){
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
                        StringBuilder newNameOfSurroundingEntity = new StringBuilder(currentlySurroundingItem.getValue().getName());

                        if (newNameOfSurroundingEntity.isEmpty()) {
                            newNameOfSurroundingEntity = new StringBuilder("Entity with hrefs: ");
                        }

                        String nameHrefContainingEntity = currentStartElement.getName().getLocalPart();
                        String nameOfReferencedEntity = referencedTreeItem.get().getValue().getName();
                        newNameOfSurroundingEntity.append(nameHrefContainingEntity).
                                append(" → ").
                                append(nameOfReferencedEntity);

                        currentlySurroundingItem.getValue().setName(newNameOfSurroundingEntity.toString());
                    }
                }
            }


        }
    }

    public @NotNull Collection<File> getModelFiles() {
        return this.modelFileParsedItemMap.keySet();
    }

    // Requires more memory this way (loading all entities) but is faster than trying to reparse specific parts of the XML-file on-demand.
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

                    // TODO: Due to href resolving being very limited, incorporate all XML elements into the tree (even those without an ID) but make only the proper one 'addable' (+ add visual clue like greying out the others)
                    Attribute href = startElement.getAttributeByName(new QName("href"));
                    if (href != null && currentlySurroundingItem != null && !currentlySurroundingItem.getValue().hasOwnName()) {
                        this.tryToResolveHref(startElement, href, currentlySurroundingItem);
                    }

                    if (id != null) {
                        var newTreeItem = new TreeItem<>(
                                new ModelEntity(
                                        id.getValue(),
                                        modelFile.getName(), // Each view is specified by its own file.
                                        startElement.getName().getLocalPart(),
                                        name == null ? null : name.getValue(),
                                        type == null ? null : type.getValue()));

                        if (currentlySurroundingItem != null) {
                            currentlySurroundingItem.getChildren().add(newTreeItem);
                        }
                        currentlySurroundingItem = newTreeItem;
                    }
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
