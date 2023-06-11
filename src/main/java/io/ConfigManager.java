package io;

import general.Assumption;
import general.Configuration;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.HashSet;
import java.util.UUID;

public class ConfigManager {
    private static void writeIsolatedElement(XMLStreamWriter streamWriter, String elementName, String value) throws XMLStreamException {
        streamWriter.writeStartElement(elementName);
        streamWriter.writeCharacters(value);
        streamWriter.writeEndElement();
    }

    public static Configuration readConfig(File target) throws FileNotFoundException, XMLStreamException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = null;

        var readConfiguration = new Configuration();
        Assumption currentAssumption = null;

        try {
            eventReader = inputFactory.createXMLEventReader(new FileInputStream(target));

            parseLoop:
            while (eventReader.hasNext()) {
                XMLEvent nextEvent = eventReader.nextEvent();
                if (nextEvent.isStartElement()) {
                    StartElement startElement = nextEvent.asStartElement();
                    switch (startElement.getName().getLocalPart()) {
                        case "analysisPath" -> {
                            nextEvent = eventReader.nextEvent();
                            readConfiguration.setAnalysisPath(nextEvent.asCharacters().getData());
                        }
                        case "modelName" -> {
                            nextEvent = eventReader.nextEvent();
                            readConfiguration.setModelPath(nextEvent.asCharacters().getData());
                        }
                        case "assumptions" -> readConfiguration.setAssumptions(new HashSet<>());
                        case "assumption" -> {
                            Attribute assumptionID = startElement.getAttributeByName(new QName("id"));
                            if (assumptionID != null) {
                                currentAssumption = new Assumption(UUID.fromString(assumptionID.getValue()));
                            }
                        }
                        case "type" -> {
                            if (currentAssumption != null) {
                                nextEvent = eventReader.nextEvent();
                                currentAssumption.setType(Assumption.AssumptionType.valueOf(nextEvent.asCharacters().getData()));
                            }
                        }
                        case "affectedEntity" -> {
                            if (currentAssumption != null) {
                                nextEvent = eventReader.nextEvent();
                                currentAssumption.setAffectedEntity(nextEvent.asCharacters().getData());
                            }
                        }
                        case "dependencies" -> {
                            if (currentAssumption != null) {
                                currentAssumption.setDependencies(new HashSet<>());
                            }
                        }
                        case "dependency" -> {
                            if (currentAssumption != null && currentAssumption.getDependencies() != null) {
                                nextEvent = eventReader.nextEvent();
                                currentAssumption.getDependencies().add(UUID.fromString(nextEvent.asCharacters().getData()));
                            }
                        }
                        case "description" -> {
                            if (currentAssumption != null) {
                                nextEvent = eventReader.nextEvent();
                                currentAssumption.setDescription(nextEvent.asCharacters().getData());
                            }
                        }
                        case "probabilityOfViolation" -> {
                            if (currentAssumption != null) {
                                nextEvent = eventReader.nextEvent();
                                currentAssumption.setProbabilityOfViolation(Double.parseDouble(nextEvent.asCharacters().getData()));
                            }
                        }
                        case "risk" -> {
                            if (currentAssumption != null) {
                                nextEvent = eventReader.nextEvent();
                                currentAssumption.setRisk(Double.parseDouble(nextEvent.asCharacters().getData()));
                            }
                        }
                        case "impact" -> {
                            if (currentAssumption != null) {
                                nextEvent = eventReader.nextEvent();
                                currentAssumption.setImpact(nextEvent.asCharacters().getData());
                            }
                        }
                        case "analyzed" -> {
                            if (currentAssumption != null) {
                                nextEvent = eventReader.nextEvent();
                                currentAssumption.setAnalyzed(Boolean.valueOf(nextEvent.asCharacters().getData()));
                            }
                        }
                    }
                }
                if (nextEvent.isEndElement()) {
                    EndElement endElement = nextEvent.asEndElement();
                    switch (endElement.getName().getLocalPart()) {
                        case "configuration" -> {
                            break parseLoop;
                        }
                        case "assumption" -> {
                            readConfiguration.getAssumptions().add(currentAssumption);
                            currentAssumption = null;
                        }
                    }
                }
            }
        } finally {
            if(eventReader != null){
                eventReader.close();
            }
        }

        return readConfiguration;
    }

    public static void writeConfig(File target, Configuration configuration) throws FileNotFoundException, XMLStreamException {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter streamWriter = null;

        try {
            streamWriter = outputFactory.createXMLStreamWriter(new FileOutputStream(target));
            streamWriter.writeStartDocument();
            streamWriter.writeStartElement("configuration");

            // Write Configuration::analysisPath
            ConfigManager.writeIsolatedElement(streamWriter, "analysisPath", configuration.getAnalysisPath());
            // Write Configuration::modelName
            ConfigManager.writeIsolatedElement(streamWriter, "modelName", configuration.getModelPath());

            // Write Configuration::assumptions
            streamWriter.writeStartElement("assumptions");
            for (var assumption : configuration.getAssumptions()) {
                streamWriter.writeStartElement("assumption");

                // Write Assumption::id
                streamWriter.writeAttribute("id", assumption.getId().toString());

                // Write Assumption::type
                ConfigManager.writeIsolatedElement(streamWriter, "type", assumption.getType().toString());

                // Write Assumption::affectedEntity
                ConfigManager.writeIsolatedElement(streamWriter, "affectedEntity", assumption.getAffectedEntity());

                // Write Assumption::dependencies
                streamWriter.writeStartElement("dependencies");
                if (assumption.getDependencies() != null) {
                    for (var dependency : assumption.getDependencies()) {
                        ConfigManager.writeIsolatedElement(streamWriter, "dependency", dependency.toString());
                    }
                }
                streamWriter.writeEndElement();

                // Write Assumption::description
                ConfigManager.writeIsolatedElement(streamWriter, "description", assumption.getDescription());

                // Write Assumption::probabilityOfViolation
                ConfigManager.writeIsolatedElement(streamWriter, "probabilityOfViolation", String.valueOf(assumption.getProbabilityOfViolation()));
                // Write Assumption::risk
                ConfigManager.writeIsolatedElement(streamWriter, "risk", String.valueOf(assumption.getRisk()));
                // Write Assumption::impact
                ConfigManager.writeIsolatedElement(streamWriter, "impact", assumption.getImpact());
                // Write Assumption::analyzed
                ConfigManager.writeIsolatedElement(streamWriter, "analyzed", String.valueOf(assumption.isAnalyzed()));

                streamWriter.writeEndElement();
            }
            streamWriter.writeEndElement(); // End Assumptions
            streamWriter.writeEndElement(); // End Configuration
        } finally {
            if(streamWriter != null) {
                streamWriter.flush();
                streamWriter.close();
            }
        }

    }
}
