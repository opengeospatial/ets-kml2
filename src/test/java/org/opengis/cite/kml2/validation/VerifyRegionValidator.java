package org.opengis.cite.kml2.validation;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.kml2.KML2;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the RegionValidator class.
 */
public class VerifyRegionValidator {

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
		URL url = this.getClass().getResource("/kml23/Region-001.xml");
		Document region = docBuilder.parse(url.toString());
		RegionValidator iut = new RegionValidator();
		boolean isValid = iut.validExtent(region.getDocumentElement());
		assertTrue("Expected valid Region.", isValid);
		assertTrue("Expected no errors.", iut.getErrors().isEmpty());
	}

	@Test
	public void invalidRegionExtent_nonuniqueLongitudes() throws SAXException,
			IOException {
		URL url = this.getClass().getResource("/kml23/Region-002.xml");
		Document region = docBuilder.parse(url.toString());
		RegionValidator iut = new RegionValidator();
		assertFalse("Expected invalid Region.",
				iut.validExtent(region.getDocumentElement()));
		assertTrue("Unexpected error message.",
				iut.getErrors().contains("uniqueness"));
	}

	@Test
	public void getLatLonAltBoxProperties() throws SAXException, IOException {
		URL url = this.getClass().getResource("/kml23/Region-001.xml");
		Document region = docBuilder.parse(url.toString());
		Node box = region.getElementsByTagNameNS(KML2.NS_NAME, "LatLonAltBox")
				.item(0);
		RegionValidator iut = new RegionValidator();
		Map<String, Double> boxProperties = iut.getNumericProperties(box, null);
		assertEquals("Unexpected value for 'east' edge", 28.125,
				boxProperties.get("east"), 0.001);
	}

}
