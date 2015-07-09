package org.opengis.cite.kml2.c2;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ETSAssert;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.util.XMLUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/**
 * Checks CL2 constraints that apply to a overlay features (kml:GroundOverlay,
 * kml:ScreenOverlay, kml:PhotoOverlay)
 * 
 * @see "OGC 14-068r1, OGC KML 2.3 - Abstract Test Suite: Conformance Level 2"
 */
public class CL2OverlayTests extends CommonFixture {

	/**
	 * Finds elements that substitute for kml:AbstractOverlayGroup elements and
	 * do not appear in an update context. If none are found, all test methods
	 * defined in the class are skipped.
	 */
	@BeforeClass
	public void findOverlayElements() {
		findTargetElements("GroundOverlay", "ScreenOverlay", "PhotoOverlay");
	}

	/**
	 * [Test] Verifies that if a kml:PhotoOverlay element includes a
	 * kml:Icon/kml:href element containing one or more tiling parameters (e.g.
	 * level, x, y), then it also includes a child kml:ImagePyramid element; the
	 * converse must also be true.
	 */
	@Test(description = "ATC-211")
	public void photoOverlayForTiledImage() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element overlay = (Element) targetElements.item(i);
			if (!overlay.getLocalName().equals("PhotoOverlay")) {
				continue;
			}
			try {
				ETSAssert
						.assertXPath(
								"not(contains(kml:Icon/kml:href, '$[')) or kml:ImagePyramid",
								overlay, null);
				ETSAssert
						.assertXPath(
								"not(kml:ImagePyramid) or contains(kml:Icon/kml:href, '$[')",
								overlay, null);
			} catch (AssertionError e) {
				// provide more informative error message
				throw new AssertionError(
						ErrorMessage
								.format(ErrorMessageKeys.CONSTRAINT_VIOLATION,
										"Tiling parameters and ImagePyramid are mutually inclusive",
										XMLUtils.buildXPointer(overlay)));
			}
		}
	}

	/**
	 * [Test] Verify that the geographic extent of a GroundOverlay feature is
	 * specified by either a kml:LatLonBox or a kml:LatLonQuad element.
	 */
	@Test(description = "ATC-212")
	public void groundOverlayExtent() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element overlay = (Element) targetElements.item(i);
			if (!overlay.getLocalName().equals("GroundOverlay")) {
				continue;
			}
			try {
				ETSAssert.assertXPath("kml:LatLonBox or kml:LatLonQuad",
						overlay, null);
			} catch (AssertionError e) {
				// provide more informative error message
				throw new AssertionError(ErrorMessage.format(
						ErrorMessageKeys.CONSTRAINT_VIOLATION,
						"Expected kml:LatLonBox or kml:LatLonQuad",
						XMLUtils.buildXPointer(overlay)));
			}
		}
	}

}
