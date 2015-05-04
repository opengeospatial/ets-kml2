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
 * Verifies the test methods in the LinearRingTests class.
 */
public class VerifyLinearRingTests {

	private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();
	private static DocumentBuilder docBuilder;
	private static ITestContext testContext;
	private static ISuite suite;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyLinearRingTests() {
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
	public void openRingInPlacemark() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("LinearRing is not closed");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml23/Placemark-002.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		LinearRingTests iut = new LinearRingTests();
		iut.initCommonFixture(testContext);
		iut.findLinearRingElements();
		iut.validLinearRingCoordinates();
	}

}
