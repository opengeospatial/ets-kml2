package org.opengis.cite.kml2.c1;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.kml2.SuiteAttribute;
import org.opengis.cite.kml2.util.ValidationUtils;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the KmlResourceTests class. Test stubs replace
 * fixture constituents where appropriate.
 */
public class VerifyKmlResourceTests {

	private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();
	private static DocumentBuilder docBuilder;
	private static ITestContext testContext;
	private static ISuite suite;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyKmlResourceTests() {
	}

	@BeforeClass
	public static void setUpFixture() throws Exception {
		testContext = mock(ITestContext.class);
		suite = mock(ISuite.class);
		when(testContext.getSuite()).thenReturn(suite);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		docBuilder = dbf.newDocumentBuilder();
		Schema kml22Schema = ValidationUtils.createKMLSchema("2.2");
		when(suite.getAttribute(SuiteAttribute.KML22_SCHEMA.getName()))
				.thenReturn(kml22Schema);
		Schema kml23Schema = ValidationUtils.createKMLSchema("2.3");
		when(suite.getAttribute(SuiteAttribute.KML23_SCHEMA.getName()))
				.thenReturn(kml23Schema);
	}

	@Test
	public void invalidKML22Document() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("cvc-complex-type.2.4.a: Invalid content was found");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml22/Placemark-001.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		KmlResourceTests iut = new KmlResourceTests();
		iut.initCommonFixture(testContext);
		iut.getKMLSchemas(testContext);
		iut.isSchemaValid();
	}

	@Test
	public void invalidKML23Document() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("cvc-assertion: Assertion evaluation");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml23/Placemark-001.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		KmlResourceTests iut = new KmlResourceTests();
		iut.initCommonFixture(testContext);
		iut.getKMLSchemas(testContext);
		iut.isSchemaValid();
	}
}
