package org.opengis.cite.kml2.c2;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ETSAssert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/**
 * Checks CL2 constraints that apply to a kml:LinearRing element.
 * 
 * @see "OGC 14-068r1, OGC KML 2.3 - Abstract Test Suite: Conformance Level 2"
 */
public class CL2LinearRingTests extends CommonFixture {

	/**
	 * Finds kml:LinearRing elements in the KML document that do not appear in
	 * an update context. If none are found, all test methods defined in the
	 * class are skipped.
	 */
	@BeforeClass
	public void findLinearRingElements() {
		findTargetElements("LinearRing");
	}

	/**
	 * [Test] Verifies that a kml:LinearRing element that constitutes the
	 * boundary of a polygon does not contain any of the following elements:
	 * kml:extrude, kml:tesselate, or kml:altitudeMode.
	 */
	@Test(description = "ATC-207")
	public void linearRingInPolygonBoundary() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element ring = (Element) targetElements.item(i);
			if (ring.getParentNode().getLocalName().endsWith("BoundaryIs")) {
				ETSAssert
						.assertXPath(
								"not(kml:extrude or kml:tesselate or kml:altitudeMode)",
								ring, null);
			}
		}
	}

}
