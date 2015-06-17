package org.opengis.cite.kml2;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class VerifyETSAssert {

	private static final String WADL_NS = "http://wadl.dev.java.net/2009/02";
	private static DocumentBuilder docBuilder;
	private static SchemaFactory factory;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyETSAssert() {
	}

	@BeforeClass
	public static void setUpClass() throws ParserConfigurationException {
		factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		docBuilder = dbf.newDocumentBuilder();
	}

	@Test
	public void validateUsingSchemaHints_expect2Errors() throws SAXException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("2 schema validation error(s) detected");
		URL url = this.getClass().getResource("/Gamma.xml");
		Schema schema = factory.newSchema();
		Validator validator = schema.newValidator();
		ETSAssert
				.assertSchemaValid(validator, new StreamSource(url.toString()));
	}

	@Test
	public void assertXPathWithNamespaceBindings() throws SAXException,
			IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/capabilities-simple.xml"));
		Map<String, String> nsBindings = new HashMap<String, String>();
		nsBindings.put(WADL_NS, "ns1");
		String xpath = "//ns1:resources";
		ETSAssert.assertXPath(xpath, doc, nsBindings);
	}

	@Test
	public void updateTargetIsRelative() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("The URI is not absolute");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml23/NetworkLinkControl-004.xml"));
		Node uriRef = doc.getElementsByTagNameNS(KML2.NS_NAME, "targetHref")
				.item(0);
		ETSAssert.assertReferentExists(uriRef, MediaType.APPLICATION_XML_TYPE);
	}

	@Test
	@Ignore("Works as expected, but avoid network connection")
	public void updateTargetExistsAndIsAcceptable() throws SAXException,
			IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml23/NetworkLinkControl-005.xml"));
		Node uriRef = doc.getElementsByTagNameNS(KML2.NS_NAME, "targetHref")
				.item(0);
		ETSAssert.assertReferentExists(uriRef,
				MediaType.valueOf(KML2.KML_MEDIA_TYPE));
	}

	@Test
	@Ignore("Works as expected, but avoid network connection")
	public void updateTargetIsNotAcceptable() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Unacceptable media type");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml23/NetworkLinkControl-005.xml"));
		Node uriRef = doc.getElementsByTagNameNS(KML2.NS_NAME, "targetHref")
				.item(0);
		ETSAssert.assertReferentExists(uriRef, MediaType.TEXT_PLAIN_TYPE);
	}

}
