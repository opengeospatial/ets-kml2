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
import org.opengis.cite.kml2.KML2;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the ViewpointValidator class.
 */
public class VerifyViewpointValidator {

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
	public void cameraMissingAltitudeMode() throws SAXException, IOException {
		URL url = this.getClass().getResource("/kml23/Placemark-Camera.xml");
		Document doc = docBuilder.parse(url.toString());
		Node camera = doc.getDocumentElement()
				.getElementsByTagNameNS(KML2.NS_NAME, "Camera").item(0);
		ViewpointValidator iut = new ViewpointValidator();
		boolean isValid = iut.isValid(camera);
		assertFalse("Expected invalid Camera.", isValid);
		assertTrue(
				"Expected error message with 'Expected altitudeMode ne 'clampToGround''",
				iut.getErrorMessages().contains(
						"Expected altitudeMode ne 'clampToGround'"));
	}

}
