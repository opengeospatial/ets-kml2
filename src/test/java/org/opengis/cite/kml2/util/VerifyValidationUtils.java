package org.opengis.cite.kml2.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.ValidatorHandler;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.validation.SchematronValidator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Verifies the behavior of the ValidationUtils class.
 */
public class VerifyValidationUtils {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyValidationUtils() {
	}

	@Test
	public void testBuildSchematronValidator() {
		String schemaRef = "http://schemas.opengis.net/gml/3.2.1/SchematronConstraints.xml";
		String phase = "";
		SchematronValidator result = ValidationUtils.buildSchematronValidator(
				schemaRef, phase);
		assertNotNull(result);
	}

	@Test
	public void extractRelativeSchemaReference() throws FileNotFoundException,
			XMLStreamException {
		File xmlFile = new File("src/test/resources/Alpha-1.xml");
		Set<URI> xsdSet = ValidationUtils.extractSchemaReferences(
				new StreamSource(xmlFile), null);
		URI schemaURI = xsdSet.iterator().next();
		assertTrue("Expected schema reference */xsd/alpha.xsd", schemaURI
				.toString().endsWith("/xsd/alpha.xsd"));
	}

	@Test
	public void buildKML22SchemaAndParseInvalidDocument() throws SAXException,
			IOException {
		thrown.expect(SAXParseException.class);
		thrown.expectMessage("cvc-complex-type.2.4.a: Invalid content was found");
		Schema schema = ValidationUtils.createKMLSchema("2015.04");
		assertNotNull(schema);
		ValidatorHandler vHandler = schema.newValidatorHandler();
		XMLReader parser = XMLReaderFactory.createXMLReader();
		parser.setContentHandler(vHandler);
		InputStream byteStream = getClass().getResourceAsStream(
				"/kml22/Placemark-001.xml");
		parser.parse(new InputSource(byteStream));
	}

	@Test
	public void buildKML23SchemaAndParseInvalidDocument() throws SAXException,
			IOException {
		thrown.expect(SAXParseException.class);
		thrown.expectMessage("cvc-assertion: Assertion evaluation");
		Schema schema = ValidationUtils.createKMLSchema("2.3");
		assertNotNull(schema);
		ValidatorHandler vHandler = schema.newValidatorHandler();
		XMLReader parser = XMLReaderFactory.createXMLReader();
		parser.setContentHandler(vHandler);
		InputStream byteStream = getClass().getResourceAsStream(
				"/kml23/Placemark-001.xml");
		parser.parse(new InputSource(byteStream));
	}
}
