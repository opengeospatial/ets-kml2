package org.opengis.cite.kml2.validation;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the LinkValidator class.
 */
public class VerifyLinkValidator {

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
	@Ignore("Works as expected, but requires a network connection")
	public void validRemoteImageLink() throws SAXException, IOException {
		Document link = docBuilder.parse(this.getClass().getResourceAsStream(
				"/links/Icon-001.xml"));
		MediaType imageType = MediaType.valueOf("image/*");
		LinkValidator iut = new LinkValidator(imageType);
		assertTrue("Expected valid Link.",
				iut.isValid(link.getDocumentElement()));
		assertTrue("Expected no errors.", iut.getErrors().hasNext());
	}

	@Test
	public void validLocalImageLink() throws SAXException, IOException {
		URL url = this.getClass().getResource("/links/Icon-002.xml");
		Document link = docBuilder.parse(url.toString());
		MediaType imageType = MediaType.valueOf("image/*");
		LinkValidator iut = new LinkValidator(imageType);
		boolean isValid = iut.isValid(link.getDocumentElement());
		assertTrue("Expected valid Link.", isValid);
		assertFalse("Expected no errors.", iut.getErrors().hasNext());
	}

	@Test
	public void localReferentNotFound() throws SAXException, IOException {
		URL url = this.getClass().getResource("/links/Link-001.xml");
		Document link = docBuilder.parse(url.toString());
		MediaType imageType = MediaType.valueOf("model/*");
		LinkValidator iut = new LinkValidator(imageType);
		boolean isValid = iut.isValid(link.getDocumentElement());
		assertFalse("Expected invalid Link.", isValid);
		assertTrue("Unexpected error message.", iut.getErrorMessages()
				.contains("URI is not accessible"));
	}

}
