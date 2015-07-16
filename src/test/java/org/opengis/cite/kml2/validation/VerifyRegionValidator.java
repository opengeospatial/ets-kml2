package org.opengis.cite.kml2.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
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
	public void validRegion() throws SAXException, IOException {
		URL url = this.getClass().getResource("/extents/Region-001.xml");
		Document region = docBuilder.parse(url.toString());
		RegionValidator iut = new RegionValidator(3);
		boolean isValid = iut.isValid(region.getDocumentElement());
		assertTrue("Expected valid Link.", isValid);
	}

	@Test
	public void invalidLodFadeRange() throws SAXException, IOException {
		URL url = this.getClass().getResource("/extents/Region-002.xml");
		Document region = docBuilder.parse(url.toString());
		RegionValidator iut = new RegionValidator(3);
		boolean isValid = iut.isValid(region.getDocumentElement());
		assertFalse("Expected inivalid Link.", isValid);
		assertTrue("Expected error message to contain 'ATC-306'", iut
				.getErrorMessages().contains("ATC-306"));
	}

}
