package org.opengis.cite.kml2.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the GeoExtentValidator class.
 */
public class VerifyGeoExtentValidator {

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
	public void validRegionExtent() throws SAXException, IOException {
		URL url = this.getClass().getResource("/extents/Box-001.xml");
		Document box = docBuilder.parse(url.toString());
		GeoExtentValidator iut = new GeoExtentValidator();
		boolean isValid = iut.validateBox(box.getDocumentElement());
		assertTrue("Expected valid LatLonAltBox.", isValid);
		assertTrue("Expected no errors.", iut.getErrors().isEmpty());
	}

	@Test
	public void invalidRegionExtent_nonuniqueLongitudes() throws SAXException,
			IOException {
		URL url = this.getClass().getResource("/extents/Box-002.xml");
		Document box = docBuilder.parse(url.toString());
		GeoExtentValidator iut = new GeoExtentValidator();
		assertFalse("Expected invalid LatLonAltBox.",
				iut.validateBox(box.getDocumentElement()));
		assertTrue("Unexpected error message.",
				iut.getErrors().contains("uniqueness"));
	}

	@Test
	public void getLatLonAltBoxProperties() throws SAXException, IOException {
		URL url = this.getClass().getResource("/extents/Box-001.xml");
		Document box = docBuilder.parse(url.toString());
		GeoExtentValidator iut = new GeoExtentValidator();
		Map<String, Double> boxProperties = iut.getNumericProperties(
				box.getDocumentElement(), null);
		assertEquals("Unexpected value for 'east' edge", 28.125,
				boxProperties.get("east"), 0.001);
	}

}
