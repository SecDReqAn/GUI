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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
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

    public @NotNull Collection<File> getModelFiles() {
        return this.modelFileParsedItemMap.keySet();
    }

    // Requires more memory this way (loading all entities) but is faster than trying to reparse specific parts of the XML-file on-demand.
    public @Nullable TreeItem<ModelEntity> readFromModelFile(@NotNull File modelFile) {
        // Only allow reading from a file within the specified model folder.
        if(!this.modelFileParsedItemMap.containsKey(modelFile)){
            return null;
        }

        // Check whether input file has already been read.
        if (this.modelFileParsedItemMap.get(modelFile) != null) {
            return this.modelFileParsedItemMap.get(modelFile);
        }

        TreeItem<ModelEntity> currentItem = null;
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

                    // TODO When encountering an element without a name but with hrefs, look up the element specified in the href and build a name.
                    Attribute href = startElement.getAttributeByName(new QName("href"));
                    if(href != null){
                        String[] hrefComponents = href.getValue().split("#");
                        if(hrefComponents.length == 2){
                            String hrefFile = hrefComponents[0];
                            String hrefId = hrefComponents[1];

                            System.out.println("Href within " + currentItem.getValue().id() + ": " + startElement.getName().getLocalPart() + " --> " + hrefComponents[0] + "::" + hrefComponents[1]);


                            // Check whether file specified one of the model files.
                            // TODO
                        }
                    }

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

                    if (currentItem.getParent() != null) {
                        currentItem = currentItem.getParent(); // Traverse up.
                    }
                }
            }

        } catch (XMLStreamException | IOException ignored) {
        }

        this.modelFileParsedItemMap.put(modelFile, currentItem);
        return currentItem;
    }
}
