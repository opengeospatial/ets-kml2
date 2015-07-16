package org.opengis.cite.kml2.c3;

import javax.xml.xpath.XPathConstants;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.util.XMLUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/**
 * Checks CL3 constraints that apply to a kml:Model element.
 * 
 * @see "OGC 14-068r1, OGC KML 2.3 - Abstract Test Suite: Conformance Level 3"
 */
public class CL3ModelTests extends CommonFixture {

	/**
	 * Finds kml:Model elements in the KML document that do not appear in an
	 * update context. If none are found, all test methods defined in the class
	 * are skipped.
	 */
	@BeforeClass
	public void findModelElements() {
		findTargetElements("Model");
	}

	/**
	 * [Test] Verifies that a kml:Scale element includes all of the following
	 * child elements: kml:x, kml:y, and kml:z.
	 */
	@Test(description = "ATC-305")
	public void allScalingFactors() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element model = (Element) targetElements.item(i);
			Boolean allFactors = (Boolean) XMLUtils.evaluateXPath(model,
					"kml:Scale[kml:x and kml:y and kml:z]", null,
					XPathConstants.BOOLEAN);
			if (!allFactors) {
				throw new AssertionError(ErrorMessage.format(
						ErrorMessageKeys.CONSTRAINT_VIOLATION,
						"Expected kml:Scale[kml:x and kml:y and kml:z]",
						XMLUtils.buildXPointer(model)));
			}
		}
	}

	/**
	 * [Test] Verifies that a that a kml:Orientation element includes the
	 * following child elements: kml:heading, kml:tilt, and kml:roll.
	 */
	@Test(description = "ATC-307")
	public void allOrientationAngles() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element model = (Element) targetElements.item(i);
			Boolean allAngles = (Boolean) XMLUtils.evaluateXPath(model,
					"kml:Orientation[kml:heading and kml:tilt and kml:roll]",
					null, XPathConstants.BOOLEAN);
			if (!allAngles) {
				throw new AssertionError(
						ErrorMessage
								.format(ErrorMessageKeys.CONSTRAINT_VIOLATION,
										"Expected kml:Orientation[kml:heading and kml:tilt and kml:roll]",
										XMLUtils.buildXPointer(model)));
			}
		}
	}
}
