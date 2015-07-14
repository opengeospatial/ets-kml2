package org.opengis.cite.kml2;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the CommonFeatureTests class.
 */
public class VerifyCommonFeatureTests {

	private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();
	private static final String LVL = SuiteAttribute.LEVEL.getName();
	private static DocumentBuilder docBuilder;
	private static ITestContext testContext;
	private static ISuite suite;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyCommonFeatureTests() {
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
	public void validLocalStyleReference() throws SAXException, IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml22/PlacemarkStyle-003.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		when(suite.getAttribute(SuiteAttribute.SHARED_STYLES.getName()))
				.thenReturn(Collections.singleton("sn_blue-dot_copy3"));
		CommonFeatureTests iut = new CommonFeatureTests();
		iut.initCommonFixture(testContext);
		iut.setTargetElements(doc.getElementsByTagNameNS(KML2.NS_NAME,
				"Placemark"));
		iut.getSharedStyles(testContext);
		iut.validStyleReference();
	}

	@Test
	public void validRemoteStyleReference() throws SAXException, IOException,
			URISyntaxException {
		URL url = this.getClass().getResource("/kml22/Placemark-004.xml");
		Document doc = docBuilder.parse(url.toString());
		doc.setDocumentURI(url.toString());
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		CommonFeatureTests iut = new CommonFeatureTests();
		iut.initCommonFixture(testContext);
		iut.setTargetElements(doc.getElementsByTagNameNS(KML2.NS_NAME,
				"Placemark"));
		iut.validStyleReference();
	}

	@Test
	public void invalidRemoteStyleReference() throws SAXException, IOException,
			URISyntaxException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Node has unexpected QName: {http://www.opengis.net/kml/2.2}Document");
		URL url = this.getClass().getResource("/kml22/Placemark-005.xml");
		Document doc = docBuilder.parse(url.toString());
		doc.setDocumentURI(url.toString());
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		CommonFeatureTests iut = new CommonFeatureTests();
		iut.initCommonFixture(testContext);
		iut.setTargetElements(doc.getElementsByTagNameNS(KML2.NS_NAME,
				"Placemark"));
		iut.validStyleReference();
	}

	@Test
	public void invalidLocalStyleReference() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("No matching shared style found for styleUrl");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml22/PlacemarkStyle-003.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		when(suite.getAttribute(SuiteAttribute.SHARED_STYLES.getName()))
				.thenReturn(Collections.singleton("no-match"));
		CommonFeatureTests iut = new CommonFeatureTests();
		iut.initCommonFixture(testContext);
		iut.setTargetElements(doc.getElementsByTagNameNS(KML2.NS_NAME,
				"Placemark"));
		iut.getSharedStyles(testContext);
		iut.validStyleReference();
	}

	@Test
	public void incompleteStyleMap() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("2 schema validation error(s) detected");
		thrown.expectMessage("Expected kml:styleURL or kml:Style element in every kml:Pair");
		thrown.expectMessage("Expected atom:link with @rel = 'related'");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml22/Document-001.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		when(suite.getAttribute(LVL)).thenReturn(2);
		CommonFeatureTests iut = new CommonFeatureTests();
		iut.initCommonFixture(testContext);
		iut.setTargetElements(doc.getElementsByTagNameNS(KML2.NS_NAME,
				"Document"));
		iut.checkFeatureConstraints();
	}
}
