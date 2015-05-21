package org.opengis.cite.kml2.c1;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.kml2.SuiteAttribute;
import org.opengis.cite.kml2.util.XMLUtils;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Verifies the test methods in the TimeSpanTests class.
 */
public class VerifyTimeSpanTests {

	private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();
	private static DocumentBuilder docBuilder;
	private static ITestContext testContext;
	private static ISuite suite;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyTimeSpanTests() {
	}

	@BeforeClass
	public static void setUpFixture() throws Exception {
		testContext = mock(ITestContext.class);
		suite = mock(ISuite.class);
		when(testContext.getSuite()).thenReturn(suite);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		docBuilder = dbf.newDocumentBuilder();
	}

	@Test
	public void validTimeSpan() throws SAXException, IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/time/TimeSpan-valid.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		TimeSpanTests iut = new TimeSpanTests();
		iut.initCommonFixture(testContext);
		iut.findTimeSpanElements();
		iut.validTimeSpan();
	}

	@Test
	public void invalidTimeSpan() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("kml:end is not after kml:begin");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/time/TimeSpan-invalid.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		TimeSpanTests iut = new TimeSpanTests();
		iut.initCommonFixture(testContext);
		iut.findTimeSpanElements();
		iut.validTimeSpan();
	}

	@Test
	public void validDefiniteTimeInterval() throws SAXException, IOException,
			XPathExpressionException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/time/TimeSpan-valid.xml"));
		NodeList timeInstants = XMLUtils.evaluateXPath(
				doc.getDocumentElement(), "kml:begin | kml:end", null);
		TimeSpanTests iut = new TimeSpanTests();
		assertTrue("Expected valid time interval.",
				iut.isValidDefiniteTimeInterval(timeInstants));
	}

	@Test
	public void invalidDefiniteTimeInterval() throws SAXException, IOException,
			XPathExpressionException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/time/TimeSpan-invalid.xml"));
		NodeList timeInstants = XMLUtils.evaluateXPath(
				doc.getDocumentElement(), "kml:begin | kml:end", null);
		TimeSpanTests iut = new TimeSpanTests();
		assertFalse("Expected invalid time interval.",
				iut.isValidDefiniteTimeInterval(timeInstants));
	}

}
