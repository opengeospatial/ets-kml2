package org.opengis.cite.kml2.c1;

import org.opengis.cite.kml2.CommonFeatureTests;
import org.opengis.cite.kml2.validation.SchemaChecker;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

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
	 * Finds kml:Document elements in the KML document that do not appear in an
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
			Element schema = (Element) targetElements.item(i);
			if (null == schema) {
				continue;
			}
			// TODO validate Schema
		}
	}
}
