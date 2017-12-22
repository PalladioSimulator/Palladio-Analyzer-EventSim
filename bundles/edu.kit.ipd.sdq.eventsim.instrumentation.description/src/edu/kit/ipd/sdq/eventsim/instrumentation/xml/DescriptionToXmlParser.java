package edu.kit.ipd.sdq.eventsim.instrumentation.xml;

import java.io.File;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;

/**
 * Provides means for saving {@link InstrumentationDescription}s to XML files.
 * 
 * @author Henning Schulz
 *
 */
public class DescriptionToXmlParser {

	private final JAXBContext context;
	private final Marshaller marshaller;
	private final Unmarshaller unmarshaller;

	public DescriptionToXmlParser() {
		try {
			context = JAXBContext.newInstance(InstrumentationDescription.class);
			marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			unmarshaller = context.createUnmarshaller();
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public void saveToFile(InstrumentationDescription description, String filename) throws JAXBException {
		marshaller.marshal(description, new File(filename));
	}

	public void printToConsole(InstrumentationDescription description) throws JAXBException {
		marshaller.marshal(description, System.out);

	}

	public InstrumentationDescription readFromInputStream(InputStream stream) throws JAXBException {
		return (InstrumentationDescription) unmarshaller.unmarshal(stream);
	}
	
	public InstrumentationDescription readFromFile(String filename) throws JAXBException {
		return (InstrumentationDescription) unmarshaller.unmarshal(new File(filename));

	}

}
