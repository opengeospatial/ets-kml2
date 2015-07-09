package org.opengis.cite.kml2.c2;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.kml2.SuiteAttribute;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the test methods in the CL2OverlayTests class.
 */
public class VerifyCL2OverlayTests {

	private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();
	private static DocumentBuilder docBuilder;
	private static ITestContext testContext;
	private static ISuite suite;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyCL2OverlayTests() {
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
	public void photoOverlayMissingImagePyramid() throws SAXException,
			IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Tiling parameters and ImagePyramid are mutually inclusive");
		URL url = this.getClass().getResource("/features/PhotoOverlay-004.xml");
		Document doc = docBuilder.parse(url.toString());
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		CL2OverlayTests iut = new CL2OverlayTests();
		iut.initCommonFixture(testContext);
		iut.findOverlayElements();
		iut.photoOverlayForLargeImage();
	}

	@Test
	public void photoOverlayOk() throws SAXException, IOException {
		URL url = this.getClass().getResource("/features/PhotoOverlay-005.xml");
		Document doc = docBuilder.parse(url.toString());
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		CL2OverlayTests iut = new CL2OverlayTests();
		iut.initCommonFixture(testContext);
		iut.findOverlayElements();
		iut.photoOverlayForLargeImage();
	}
}
