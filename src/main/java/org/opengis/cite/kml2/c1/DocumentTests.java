package org.opengis.cite.kml2.c1;

import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.opengis.cite.kml2.CommonFeatureTests;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.SuiteAttribute;
import org.opengis.cite.kml2.util.KMLUtils;
import org.opengis.cite.kml2.validation.SchemaChecker;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Implements tests that apply to the kml:Document element.
 * 
 * @see "OGC 12-007r1: OGC KML 2.3, 10.9.2"
 * @see "OGC 14-068r1: OGC KML 2.3 - Abstract Test Suite, Conformance Level 1"
 */
public class DocumentTests extends CommonFeatureTests {

	private SchemaChecker schemaChecker;

	public DocumentTests() {
		this.schemaChecker = new SchemaChecker();
	}

	/**
	 * Finds shared styles occurring in a kml:Document container. The resulting
	 * collection ({@code Set<String>}) of style identifiers is set as the value
	 * of the suite attribute {@link SuiteAttribute#SHARED_STYLES}.
	 * 
	 * @param testContext
	 *            Information about the test run.
	 */
	@BeforeSuite
	public void findSharedStyles(ITestContext testContext) {
		Document kmlDoc = (Document) testContext.getSuite().getAttribute(
				SuiteAttribute.TEST_SUBJECT.getName());
		Source kmlSource = new DOMSource(kmlDoc, kmlDoc.getBaseURI());
		Set<String> sharedStyles = KMLUtils.findElementIdentifiers(kmlSource,
				"//kml:Document/kml:Style | //kml:Document/kml:StyleMap");
		testContext.getSuite().setAttribute(
				SuiteAttribute.SHARED_STYLES.getName(), sharedStyles);
	}

	/**
	 * Finds custom schemas (kml:Schema) defined in a kml:Document container.
	 * The resulting collection ({@code Set<String>}) of schema identifiers is
	 * set as the value of the suite attribute
	 * {@link SuiteAttribute#CUSTOM_SCHEMAS}.
	 * 
	 * @param testContext
	 *            Information about the test run.
	 */
	@BeforeSuite
	public void findCustomSchemas(ITestContext testContext) {
		Document kmlDoc = (Document) testContext.getSuite().getAttribute(
				SuiteAttribute.TEST_SUBJECT.getName());
		Source kmlSource = new DOMSource(kmlDoc, kmlDoc.getBaseURI());
		Set<String> customSchemas = KMLUtils.findElementIdentifiers(kmlSource,
				"//kml:Document/kml:Schema");
		testContext.getSuite().setAttribute(
				SuiteAttribute.CUSTOM_SCHEMAS.getName(), customSchemas);
	}

	/**
	 * Finds kml:Document elements in the KML resource that do not appear in an
	 * update context. If none are found, all test methods defined in the class
	 * are skipped.
	 */
	@BeforeClass
	public void findDocumentElements() {
		findTargetElements("Document");
	}

	/**
	 * [Test] Verifies that a kml:Schema element is valid.
	 */
	@Test(description = "ATC-119")
	public void validSchema() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element doc = (Element) targetElements.item(i);
			NodeList schemaList = doc.getElementsByTagNameNS(KML2.NS_NAME,
					"Schema");
			for (int j = 0; j < schemaList.getLength(); j++) {
				Assert.assertTrue(schemaChecker.isValid(schemaList.item(j)),
						schemaChecker.getErrorMessages());
			}
		}
	}
}
