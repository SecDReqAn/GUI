package io;

import general.Assumption;
import general.Configuration;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StringWriter;

public class ConfigManager {
    private static void writeIsolatedElement(XMLStreamWriter streamWriter, String elementName, String value) throws XMLStreamException {
        streamWriter.writeStartElement(elementName);
        streamWriter.writeCharacters(value);
        streamWriter.writeEndElement();
    }

    public static void writeConfig(File target, Configuration configuration) throws FileNotFoundException, XMLStreamException {
        StringWriter stringWriter = new StringWriter();
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter streamWriter = outputFactory.createXMLStreamWriter(new FileOutputStream(target));

        streamWriter.writeStartDocument();
        streamWriter.writeStartElement("configuration");

        // Write Configuration::analysisPath
        ConfigManager.writeIsolatedElement(streamWriter, "analysisPath", configuration.getAnalysisPath());
        // Write Configuration::modelName
        ConfigManager.writeIsolatedElement(streamWriter, "modelName", configuration.getModelName());

        // Write Configuration::assumptions
        streamWriter.writeStartElement("assumptions");
        for (Assumption assumption : configuration.getAssumptions()) {
            streamWriter.writeStartElement("assumption");

            // Write Assumption::id
            streamWriter.writeAttribute("id", assumption.getId().toString());

            // Write Assumption::type
            ConfigManager.writeIsolatedElement(streamWriter, "type", assumption.getType().toString());

            // Write Assumption::dependencies
            streamWriter.writeStartElement("dependencies");
            if(assumption.getDependencies() != null) {
                for (Assumption dependency : assumption.getDependencies()) {
                    ConfigManager.writeIsolatedElement(streamWriter, "dependency", dependency.getId().toString());
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
        streamWriter.writeEndElement();

        streamWriter.writeEndDocument();
        streamWriter.close();
    }
}
