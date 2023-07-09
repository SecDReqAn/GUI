package io;

import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModelReader {
    public record ModelEntity(@NotNull String id, String elementName, String name, String type) {
        @Override
        public String toString() {
            return "ModelEntity{" +
                    "id='" + id + '\'' +
                    ", elementName='" + elementName + '\'' +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    // TODO Delete.
    public static void main(String[] args) {
        var test = ModelReader.readModel(new File("/home/tim/git/UncertaintyImpactAnalysis/tests/dev.abunai.impact.analysis.testmodels/casestudies/CaseStudy-CoronaWarnApp/CoronaWarnApp"));
        String s = "Hi";
    }

    public static @NotNull Map<String, Map<String, ModelEntity>> readModel(@NotNull File modelFolder) {
        var readEntitiesByView = new HashMap<String, Map<String, ModelEntity>>();

        var modelFiles = modelFolder.listFiles();
        if (modelFiles != null && modelFiles.length != 0) {
            // TODO: Filter out irrelevant files.
            var relevantFiles = Stream.of(modelFiles)
                    .filter(File::isFile)
                    .collect(Collectors.toSet());

            for (var relevantFile : relevantFiles) {
                readEntitiesByView.put(relevantFile.getName(), ModelReader.readFromModelFile(relevantFile));
            }
        }

        return readEntitiesByView;
    }

    private static @NotNull Map<String, ModelEntity> readFromModelFile(@NotNull File modelFile) {
        var readEntities = new HashMap<String, ModelEntity>();

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

                    if (id != null) {
                        ModelEntity newEntity = new ModelEntity(
                                id.getValue(),
                                startElement.getName().getLocalPart(),
                                name == null ? null : name.getValue(),
                                type == null ? null : type.getValue());

                        readEntities.put(newEntity.id(), newEntity);
                    }
                }
            }

        } catch (XMLStreamException | IOException ignored) {
        }

        return readEntities;
    }

    // TODO Remove + change controller to use readModel() + adapt UI.
    public static @NotNull Map<String, ModelEntity> readFromRepositoryFile(@NotNull File repositoryFile) throws FileNotFoundException, XMLStreamException {
        return ModelReader.readFromModelFile(repositoryFile);
    }
}
