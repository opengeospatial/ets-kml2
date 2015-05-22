package org.opengis.cite.kml2.c1;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.SuiteAttribute;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the KmlFeatureTests class.
 */
public class VerifyKmlFeatureTests {

	private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();
	private static DocumentBuilder docBuilder;
	private static ITestContext testContext;
	private static ISuite suite;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyKmlFeatureTests() {
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
		KmlFeatureTests iut = new KmlFeatureTests();
		iut.setTargetElements(doc.getElementsByTagNameNS(KML2.NS_NAME,
				"Placemark"));
		iut.getSharedStyles(testContext);
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
		KmlFeatureTests iut = new KmlFeatureTests();
		iut.setTargetElements(doc.getElementsByTagNameNS(KML2.NS_NAME,
				"Placemark"));
		iut.getSharedStyles(testContext);
		iut.validStyleReference();
	}
}