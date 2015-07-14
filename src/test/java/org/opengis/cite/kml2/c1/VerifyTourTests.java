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
 * Verifies the test methods in the Tour(ing)Tests class.
 */
public class VerifyTourTests {

	private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();
	private static DocumentBuilder docBuilder;
	private static ITestContext testContext;
	private static ISuite suite;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyTourTests() {
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
	public void animatedUpdate_ok() throws SAXException, IOException {
		URL url = this.getClass().getResource(
				"/kml23/Tour-AnimatedUpdate-1.xml");
		Document doc = docBuilder.parse(url.toString());
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		TourTests iut = new TourTests();
		iut.initCommonFixture(testContext);
		iut.findTourElements();
		iut.animatedUpdateTarget();
	}

	@Test
	public void animatedUpdate_invalidChange() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("KML object must not have @id in update context");
		URL url = this.getClass().getResource(
				"/kml23/Tour-AnimatedUpdate-2.xml");
		Document doc = docBuilder.parse(url.toString());
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		TourTests iut = new TourTests();
		iut.initCommonFixture(testContext);
		iut.findTourElements();
		iut.animatedUpdateTarget();
	}

	@Test
	public void generalTourConstraints() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Expected kml:playMode in kml:TourControl element");
		URL url = this.getClass().getResource(
				"/kml23/Tour-AnimatedUpdate-2.xml");
		Document doc = docBuilder.parse(url.toString());
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		TourTests iut = new TourTests();
		iut.initCommonFixture(testContext);
		iut.findTourElements();
		iut.checkTourConstraints();
	}
}
