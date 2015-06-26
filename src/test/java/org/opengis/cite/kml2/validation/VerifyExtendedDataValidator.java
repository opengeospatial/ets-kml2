package org.opengis.cite.kml2.validation;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.junit.BeforeClass;
import org.junit.Ignore;
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

	@Test
	public void fetchSchemaInSameDocument() throws SAXException, IOException,
			SaxonApiException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml22/Placemark-SchemaData-1.xml"));
		ExtendedDataValidator iut = new ExtendedDataValidator();
		iut.setOwnerDocument(doc);
		XdmNode schema = iut.fetchSchema(URI.create("#TrailHead"));
		assertNotNull("Schema not found.", schema);
		assertEquals("Schema has unexpected name.", "TrailHeadSchema",
				schema.getAttributeValue(new QName("name")));
	}

	@Test
	@Ignore("Works as expected, but avoid network access")
	public void fetchRemoteSchema() throws SAXException, IOException,
			SaxonApiException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml22/Placemark-SchemaData.xml"));
		ExtendedDataValidator iut = new ExtendedDataValidator();
		iut.setOwnerDocument(doc); // required but irrelevant for remote schema
		URI uri = URI
				.create("https://kml-samples.googlecode.com/svn/trunk/kml/Schema/schemadata-trailhead.kml#TrailHeadTypeId");
		XdmNode schema = iut.fetchSchema(uri);
		assertNotNull("Schema not found.", schema);
		assertEquals("Schema has unexpected name.", "TrailHeadType",
				schema.getAttributeValue(new QName("name")));
	}

	@Test
	public void checkSchemaData() throws SAXException, IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml22/Placemark-SchemaData-1.xml"));
		Element extData = (Element) doc.getElementsByTagNameNS(KML2.NS_NAME,
				"ExtendedData").item(0);
		ExtendedDataValidator iut = new ExtendedDataValidator();
		iut.setOwnerDocument(doc);
		iut.checkSchemaData(extData);
		assertTrue("Unexpected error.", iut.getErrorMessages().isEmpty());
	}

	@Test
	public void checkSchemaDataWithInvalidReference() throws SAXException,
			IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml22/Placemark-SchemaData-2.xml"));
		Element extData = (Element) doc.getElementsByTagNameNS(KML2.NS_NAME,
				"ExtendedData").item(0);
		ExtendedDataValidator iut = new ExtendedDataValidator();
		iut.checkSchemaData(extData);
		assertFalse("Expected an error.", iut.getErrorMessages().isEmpty());
		assertTrue(
				"Expected error message to contain 'Resource not found: #TrailHeadSchema'",
				iut.getErrorMessages().contains(
						"Resource not found: #TrailHeadSchema"));
	}
}
