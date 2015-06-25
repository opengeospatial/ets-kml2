package org.opengis.cite.kml2.validation;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.kml2.KML2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the SchemaChecker class.
 */
public class VerifySchemaChecker {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private static DocumentBuilder docBuilder;

	@BeforeClass
	public static void setUpClass() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		docBuilder = dbf.newDocumentBuilder();
	}

	@Test
	public void checkUnit_kilometer() {
		Document doc = docBuilder.newDocument();
		Element simpleField = doc.createElementNS(KML2.NS_NAME, "SimpleField");
		simpleField.setAttribute("uom", "km");
		doc.appendChild(simpleField);
		SchemaChecker iut = new SchemaChecker();
		iut.checkUnitOfMeasure(simpleField);
		assertTrue("Unexpected error.", iut.getErrorMessages().isEmpty());
	}

	@Test
	public void checkUnit_nauticalMile() {
		Document doc = docBuilder.newDocument();
		Element simpleField = doc.createElementNS(KML2.NS_NAME, "SimpleField");
		simpleField.setAttribute("uom", "[nmi_i]");
		doc.appendChild(simpleField);
		SchemaChecker iut = new SchemaChecker();
		iut.checkUnitOfMeasure(simpleField);
		assertTrue("Unexpected error.", iut.getErrorMessages().isEmpty());
	}

	@Test
	public void checkUnit_unknown() {
		Document doc = docBuilder.newDocument();
		Element simpleField = doc.createElementNS(KML2.NS_NAME, "SimpleField");
		simpleField.setAttribute("uom", "msec");
		doc.appendChild(simpleField);
		SchemaChecker iut = new SchemaChecker();
		iut.checkUnitOfMeasure(simpleField);
		assertFalse("Expected an error.", iut.getErrorMessages().isEmpty());
		assertTrue("Expected error message to contain 'No definition found'",
				iut.getErrorMessages().contains("No definition found"));
	}

	@Test
	public void checkSimpleFields_builtInDatatypes() throws SAXException,
			IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/schemas/Schema-001.xml"));
		Element schema = (Element) doc.getElementsByTagNameNS(KML2.NS_NAME,
				"Schema").item(0);
		SchemaChecker iut = new SchemaChecker();
		iut.checkSimpleFields(schema);
		assertTrue("Unexpected error.", iut.getErrorMessages().isEmpty());
	}

	@Test
	public void checkSimpleFields_invalidDatatype() throws SAXException,
			IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/schemas/Schema-002.xml"));
		Element schema = (Element) doc.getElementsByTagNameNS(KML2.NS_NAME,
				"Schema").item(0);
		SchemaChecker iut = new SchemaChecker();
		iut.checkSimpleFields(schema);
		assertFalse("Expected an error.", iut.getErrorMessages().isEmpty());
		assertTrue(
				"Expected error message to contain 'Unknown atomic type'",
				iut.getErrorMessages().contains(
						"Invalid data type: Unknown atomic type"));
	}
}
