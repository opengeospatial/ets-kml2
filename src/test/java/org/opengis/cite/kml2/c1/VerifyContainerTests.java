package org.opengis.cite.kml2.c1;

import static org.mockito.Mockito.*;

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
 * Verifies the behavior of the ContainerTests class.
 */
public class VerifyContainerTests {

	private static final String SUBJ = SuiteAttribute.TEST_SUBJECT.getName();
	private static DocumentBuilder docBuilder;
	private static ITestContext testContext;
	private static ISuite suite;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyContainerTests() {
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
	public void schemaHasInvalidSimpleFieldType() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Invalid data type: Unknown atomic type");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/schemas/Schema-002.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		ContainerTests iut = new ContainerTests();
		iut.initCommonFixture(testContext);
		iut.findContainerElements();
		iut.validSchema();
	}

	@Test
	public void validFolder() throws SAXException, IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/kml22/Folder-001.xml"));
		when(suite.getAttribute(SUBJ)).thenReturn(doc);
		ContainerTests iut = new ContainerTests();
		iut.initCommonFixture(testContext);
		iut.findContainerElements();
		iut.atomAuthor();
		iut.phoneNumber();
	}
}
