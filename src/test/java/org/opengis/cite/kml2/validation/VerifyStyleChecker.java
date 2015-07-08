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
 * Verifies the behavior of the StyleChecker class.
 */
public class VerifyStyleChecker {

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
	public void iconStyleMissingIcon() throws SAXException, IOException {
		URL url = this.getClass().getResource("/styles/Style-001.xml");
		Document link = docBuilder.parse(url.toString());
		StyleChecker iut = new StyleChecker();
		boolean isValid = iut.isValid(link.getDocumentElement());
		assertFalse("Expected invalid Link.", isValid);
		assertTrue("Expected 'URI is not accessible'.", iut.getErrorMessages()
				.contains("URI is not accessible"));
	}

	@Test
	public void fetchingItemIconNotInNetworkLink() throws SAXException,
			IOException {
		URL url = this.getClass().getResource("/features/Placemark-103.xml");
		Node style = docBuilder.parse(url.toString())
				.getElementsByTagNameNS(KML2.NS_NAME, "Style").item(0);
		StyleChecker iut = new StyleChecker();
		boolean isValid = iut.isValid(style);
		assertFalse("Expected invalid style.", isValid);
		assertTrue(
				"Expected message with 'ListStyle applies to NetworkLink'",
				iut.getErrorMessages().contains(
						"ListStyle applies to NetworkLink"));
	}

}
