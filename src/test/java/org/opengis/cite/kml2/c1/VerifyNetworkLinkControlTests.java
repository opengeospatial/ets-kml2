package org.opengis.cite.kml2.c1;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;

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
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the NetworkLinkControlTests class.
 */
public class VerifyNetworkLinkControlTests {

	private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();
	private static DocumentBuilder docBuilder;
	private static ITestContext testContext;
	private static ISuite suite;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyNetworkLinkControlTests() {
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
	public void validRefreshPeriod() throws SAXException, IOException {
		URL url = this.getClass().getResource(
				"/kml23/NetworkLinkControl-001.xml");
		Document doc = docBuilder.parse(url.toString());
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		NetworkLinkControlTests iut = new NetworkLinkControlTests();
		iut.initCommonFixture(testContext);
		iut.findNetworkLinkControlElements();
		Node node = doc.getDocumentElement().getElementsByTagNameNS(KML2.NS_NAME, "*").item(0);
		iut.refreshPeriod(node);
	}

	@Test
	public void defaultRefreshPeriod() throws SAXException, IOException {
		URL url = this.getClass().getResource(
				"/kml23/NetworkLinkControl-002.xml");
		Document doc = docBuilder.parse(url.toString());
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		NetworkLinkControlTests iut = new NetworkLinkControlTests();
		iut.initCommonFixture(testContext);
		iut.findNetworkLinkControlElements();
		Node node = doc.getDocumentElement().getElementsByTagNameNS(KML2.NS_NAME, "*").item(0);
		iut.refreshPeriod(node);
	}

	@Test
	public void invalidRefreshPeriod() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Constraint not satisfied: kml:minRefreshPeriod >= 0");
		URL url = this.getClass().getResource(
				"/kml23/NetworkLinkControl-003.xml");
		Document doc = docBuilder.parse(url.toString());
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		NetworkLinkControlTests iut = new NetworkLinkControlTests();
		iut.initCommonFixture(testContext);
		iut.findNetworkLinkControlElements();
		Node node = doc.getDocumentElement().getElementsByTagNameNS(KML2.NS_NAME, "*").item(0);
		iut.refreshPeriod(node);
	}
}
