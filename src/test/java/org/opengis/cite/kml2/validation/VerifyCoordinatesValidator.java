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
import org.w3c.dom.NodeList;
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
	public void isRing() {
		String[] tuples = new String[] { "-123.25,49.26,80", "-123.26,49.33",
				"-123.11,49.29", "-123.250,49.260,80.0" };
		CoordinatesValidator iut = new CoordinatesValidator();
		assertTrue("Expected closed ring.", iut.isClosed(tuples));
	}

	@Test
	public void isNotRing() {
		String[] tuples = new String[] { "-123.25,49.26,80", "-123.26,49.33",
				"-123.11,49.29", "-123.18,49.19,4" };
		CoordinatesValidator iut = new CoordinatesValidator();
		assertFalse("Expected open ring.", iut.isClosed(tuples));
	}

	@Test
	public void validPoint() throws SAXException, IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml23/Point-001.xml"));
		NodeList coords = doc.getElementsByTagNameNS(KML2.NS_NAME,
				"coordinates");
		CoordinatesValidator iut = new CoordinatesValidator();
		assertTrue("Expected valid Point.", iut.isValid(coords.item(0)));
		assertTrue("Expected no errors.", iut.getErrors().isEmpty());
	}

	@Test
	public void invalidLinearRing() throws SAXException, IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml23/LinearRing-001.xml"));
		NodeList coords = doc.getElementsByTagNameNS(KML2.NS_NAME,
				"coordinates");
		CoordinatesValidator iut = new CoordinatesValidator();
		assertFalse("Expected invalid LinearRing.", iut.isValid(coords.item(0)));
		assertTrue("Unexpected error message.",
				iut.getErrors().contains("LinearRing must be closed"));
	}
}
