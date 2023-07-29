package io;

import general.ModelEntity;
import javafx.scene.control.TreeItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModelReader {
    public static class EntityComparator implements Comparator<TreeItem<ModelEntity>> {
        @Override
        public int compare(TreeItem<ModelEntity> first, TreeItem<ModelEntity> second) {
            ModelEntity firstEntity = first.getValue();
            ModelEntity secondEntity = second.getValue();
            if (firstEntity.name() != null && secondEntity.name() == null) {
                return -1;
            } else if (firstEntity.name() == null && secondEntity.name() != null) {
                return 1;
            } else if (firstEntity.name() != null) {
                return firstEntity.name().compareTo(secondEntity.name());
            } else {
                if (firstEntity.type() != null && secondEntity.type() == null) {
                    return -1;
                } else if (firstEntity.type() == null && secondEntity.type() != null) {
                    return 1;
                } else if (firstEntity.type() != null) {
                    return firstEntity.type().compareTo(secondEntity.type());
                } else {
                    return firstEntity.id().compareTo(secondEntity.id());
                }
            }
        }
    }

    public static record ModelEntityNode(ModelEntity entity, ModelEntity parent) {
    }

    public static @NotNull Map<String, TreeItem<ModelEntity>> readModel(@NotNull File modelFolder) {
        var readEntitiesByView = new HashMap<String, TreeItem<ModelEntity>>();

        var modelFiles = modelFolder.listFiles();
        if (modelFiles != null && modelFiles.length != 0) {
            // TODO: Filter out irrelevant files.
            var relevantFiles = Stream.of(modelFiles)
                    .filter(File::isFile)
                    .filter(file -> !file.getName().startsWith(".")) // Do not consider hidden files.
                    .collect(Collectors.toSet());

            for (var relevantFile : relevantFiles) {
                readEntitiesByView.put(relevantFile.getName(), ModelReader.readFromModelFile(relevantFile));
            }
        }

        return readEntitiesByView;
    }

    // TODO Overhaul to be memory efficient (see https://docs.oracle.com/javafx/2/api/javafx/scene/control/TreeItem.html)
    private static @Nullable TreeItem<ModelEntity> readFromModelFile(@NotNull File modelFile) {

        TreeItem<ModelEntity> currentItem = null;
        try (var fileInputStream = new FileInputStream(modelFile)) {
            var inputFactory = XMLInputFactory.newInstance();
            var eventReader = inputFactory.createXMLEventReader(fileInputStream);

            // TODO Build hierarchy from XML
            currentItem = null;

            while (eventReader.hasNext()) {
                XMLEvent nextEvent = eventReader.nextEvent();
                if (nextEvent.isStartElement()) {
                    StartElement startElement = nextEvent.asStartElement();
                    Attribute type = startElement.getAttributeByName(new QName(startElement.getNamespaceContext().getNamespaceURI("xsi"), "type", "xsi"));
                    Attribute id = startElement.getAttributeByName(new QName("id"));
                    Attribute name = startElement.getAttributeByName(new QName("entityName"));

                    if (id != null) {
                        var newTreeItem = new TreeItem<>(
                                new ModelEntity(
                                        id.getValue(),
                                        modelFile.getName(), // Each view is specified by its own file.
                                        startElement.getName().getLocalPart(),
                                        name == null ? null : name.getValue(),
                                        type == null ? null : type.getValue()));

                        if (currentItem != null) {
                            currentItem.getChildren().add(newTreeItem);
                        }
                        currentItem = newTreeItem;
                    }
                } else if (nextEvent.isEndElement() && currentItem != null) {
                    currentItem.getChildren().sort(new ModelReader.EntityComparator());

                    if(currentItem.getParent() != null){
                        currentItem = currentItem.getParent(); // Traverse up.
                    }
                }
            }

        } catch (XMLStreamException | IOException ignored) {
        }

        return currentItem;
    }
}
