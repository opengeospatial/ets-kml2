package org.opengis.cite.kml2.c1;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

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
 * Verifies the test methods in the PointTests class.
 */
public class VerifyPointTests {

	private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();
	private static DocumentBuilder docBuilder;
	private static ITestContext testContext;
	private static ISuite suite;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyPointTests() {
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
	public void validPointInPlacemark() throws SAXException, IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml22/Placemark-001.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		PointTests iut = new PointTests();
		iut.initCommonFixture(testContext);
		iut.findPointElements();
		iut.validPointCoordinates();
	}

	@Test
	public void invalidPointInPlacemark() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Geometry lies outside valid area of CRS");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml22/PlacemarkPoint-002.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		PointTests iut = new PointTests();
		iut.initCommonFixture(testContext);
		iut.findPointElements();
		iut.validPointCoordinates();
	}

	@Test
	public void invalid2DPointRelativeToGround() throws SAXException,
			IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("altitudeMode is not 'clampToGround'");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/geom/PointRelativeToGround.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		PointTests iut = new PointTests();
		iut.initCommonFixture(testContext);
		iut.findPointElements();
		iut.validPointCoordinates();
	}

}
