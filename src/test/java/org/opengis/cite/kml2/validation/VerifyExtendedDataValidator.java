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
 * Verifies the behavior of the ExtendedDataValidator class.
 */
public class VerifyExtendedDataValidator {

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
	public void extendedDataWithUnknownUoM_yd() throws SAXException,
			IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml23/ExtendedData-001.xml"));
		Element extData = (Element) doc.getElementsByTagNameNS(KML2.NS_NAME,
				"ExtendedData").item(0);
		ExtendedDataValidator iut = new ExtendedDataValidator();
		iut.checkData(extData);
		assertFalse("Expected an error.", iut.getErrorMessages().isEmpty());
		assertTrue(
				"Expected error message to contain 'No definition found for unit of measure: yd'",
				iut.getErrorMessages().contains(
						"No definition found for unit of measure: yd"));
	}

}
