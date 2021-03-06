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
import org.opengis.cite.kml2.SuiteAttribute;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the OverlayTests class.
 */
public class VerifyOverlayTests {

	private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();
	private static DocumentBuilder docBuilder;
	private static ITestContext testContext;
	private static ISuite suite;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyOverlayTests() {
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
	public void overlayIconDoesNotExist() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("URI is not accessible");
		URL url = this.getClass().getResource("/features/PhotoOverlay-002.xml");
		Document doc = docBuilder.parse(url.toString());
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		OverlayTests iut = new OverlayTests();
		iut.initCommonFixture(testContext);
		iut.findOverlayElements();
		iut.overlayIcon();
	}

	@Test
	public void quadBoundaryIsNotCCW() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("is not oriented counter-clockwise");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml23/GroundOverlay-001.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		OverlayTests iut = new OverlayTests();
		iut.initCommonFixture(testContext);
		iut.findOverlayElements();
		iut.groundOverlayExtent();
	}

	@Test
	public void quadIsNotConvex() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("is not convex");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml23/GroundOverlay-002.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		OverlayTests iut = new OverlayTests();
		iut.initCommonFixture(testContext);
		iut.findOverlayElements();
		iut.groundOverlayExtent();
	}

	@Test
	public void photoOverlayHasIncompleteView() throws SAXException,
			IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("2 schema validation error(s) detected");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/features/PhotoOverlay-003.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		OverlayTests iut = new OverlayTests();
		iut.initCommonFixture(testContext);
		iut.findOverlayElements();
		iut.checkOverlayConstraints();
	}

	@Test
	public void groundOverlayIsMissingAltitude() throws SAXException,
			IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Expected kml:altitude element when kml:altitudeMode = 'absolute'");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/features/GroundOverlay-001.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		OverlayTests iut = new OverlayTests();
		iut.initCommonFixture(testContext);
		iut.findOverlayElements();
		iut.checkOverlayConstraints();
	}

	@Test
	public void validGroundOverlay() throws SAXException, IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/features/GroundOverlay-002.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		OverlayTests iut = new OverlayTests();
		iut.initCommonFixture(testContext);
		iut.findOverlayElements();
		iut.checkOverlayConstraints();
	}
}
