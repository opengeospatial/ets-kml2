package org.opengis.cite.kml2.c1;

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ETSAssert;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.SuiteAttribute;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Checks constraints that apply to a KML resource as a whole. Specifically, it
 * must be schema-valid. The applicable schema is determined by the value of the
 * version attribute set on the document element (default value: "2.2").
 * 
 * @see "OGC 14-068r1, OGC KML 2.3 - Abstract Test Suite: Conformance Level 1"
 */
public class KmlResourceTests extends CommonFixture {

	private Map<String, Schema> kmlSchemas;

	/**
	 * Gets the KML schema grammars from the test context.
	 *
	 * @param testContext
	 *            The test context containing various suite attributes.
	 */
	@BeforeClass
	public void getKMLSchemas(ITestContext testContext) {
		this.kmlSchemas = new HashMap<String, Schema>();
		Object obj = testContext.getSuite().getAttribute(
				SuiteAttribute.KML22_SCHEMA.getName());
		kmlSchemas.put(KML2.KML_22, Schema.class.cast(obj));
		obj = testContext.getSuite().getAttribute(
				SuiteAttribute.KML23_SCHEMA.getName());
		kmlSchemas.put(KML2.KML_23, Schema.class.cast(obj));
	}

	/**
	 * [Test] Verifies that the KML document is valid with respect to the
	 * applicable XML Schema grammar (as determined by the value of the version
	 * attribute).
	 */
	@Test(description = "ATC-102")
	public void isSchemaValid() {
		Schema kmlSchema;
		String kmlVer = this.kmlDoc.getDocumentElement().getAttribute(
				KML2.VER_ATTR);
		if (kmlVer.startsWith(KML2.KML_23)) {
			kmlSchema = this.kmlSchemas.get(KML2.KML_23);
		} else {
			kmlSchema = this.kmlSchemas.get(KML2.KML_22);
		}
		Validator validator = kmlSchema.newValidator();
		Source kmlSource = new DOMSource(this.kmlDoc);
		kmlSource.setSystemId(kmlDoc.getDocumentURI());
		ETSAssert.assertSchemaValid(validator, kmlSource);
	}

}
