package io;

import general.ModelEntity;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class ModelReader {
    public static Map<String, ModelEntity> readFromRepositoryFile(File repositoryFile) throws FileNotFoundException, XMLStreamException {
        var readEntities = new HashMap<String, ModelEntity>();

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(new FileInputStream(repositoryFile));

        while (eventReader.hasNext()) {
            XMLEvent nextEvent = eventReader.nextEvent();
            if (nextEvent.isStartElement()) {
                StartElement startElement = nextEvent.asStartElement();
                switch (startElement.getName().getLocalPart()) {
                    case "components__Repository", "interfaces__Repository" -> {
                        Attribute type = startElement.getAttributeByName(new QName(startElement.getNamespaceContext().getNamespaceURI("xsi"), "type", "xsi"));
                        Attribute id = startElement.getAttributeByName(new QName("id"));
                        Attribute name = startElement.getAttributeByName(new QName("entityName"));

                        if (type != null && id != null && name != null) {
                            ModelEntity newEntity = new ModelEntity(type.getValue(), id.getValue(), name.getValue());
                            readEntities.put(newEntity.getName(), newEntity);
                        }
                    }
                }

            }
        }

        // TODO Clean-up resource.

        return readEntities;
    }
}
