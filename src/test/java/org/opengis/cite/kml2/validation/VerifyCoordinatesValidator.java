package org.opengis.cite.kml2.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the CoordinatesValidator class.
 */
public class VerifyCoordinatesValidator {

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
	public void pointWithoutCoordinates() throws SAXException, IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/geom/PointNoCoordinates.xml"));
		CoordinatesValidator iut = new CoordinatesValidator();
		assertFalse("Expected invalid Point.",
				iut.isValid(doc.getDocumentElement()));
		assertTrue("Unexpected error message.",
				iut.getErrorMessages().contains("No kml:coordinates element found"));
	}

	@Test
	public void validPoint() throws SAXException, IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/geom/Point-001.xml"));
		CoordinatesValidator iut = new CoordinatesValidator();
		assertTrue("Expected valid Point.",
				iut.isValid(doc.getDocumentElement()));
		assertTrue("Expected no errors.", iut.getErrorMessages().isEmpty());
	}

	@Test
	public void invalidLinearRing() throws SAXException, IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/geom/LinearRing-001.xml"));
		CoordinatesValidator iut = new CoordinatesValidator();
		assertFalse("Expected invalid LinearRing.",
				iut.isValid(doc.getDocumentElement()));
		assertTrue(
				"Unexpected error message.",
				iut.getErrorMessages().contains(
						"LinearRing element must contain four or more"));
	}
}
