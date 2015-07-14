package org.opengis.cite.kml2.c2;

import javax.ws.rs.core.MediaType;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ETSAssert;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.kml2.validation.LinkValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
	 * converse must also be true (i.e. they are mutually inclusive).
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
				// provide a more informative error message
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

	/**
	 * [Test] Verify that a ScreenOverlay has a child kml:screenXY element. It
	 * specifies a point relative to the screen origin that the overlay image is
	 * mapped to.
	 */
	@Test(description = "ATC-216")
	public void screenOverlayPosition() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element overlay = (Element) targetElements.item(i);
			if (!overlay.getLocalName().equals("ScreenOverlay")) {
				continue;
			}
			try {
				ETSAssert.assertXPath("kml:screenXY", overlay, null);
			} catch (AssertionError e) {
				// provide more informative error message
				throw new AssertionError(ErrorMessage.format(
						ErrorMessageKeys.CONSTRAINT_VIOLATION,
						"Expected kml:screenXY in ScreenOverlay",
						XMLUtils.buildXPointer(overlay)));
			}
		}
	}

	/**
	 * [Test] Verify that an overlay feature contains a kml:Icon child element
	 * that refers to an image resource.
	 */
	@Test(description = "ATC-215")
	public void overlayImage() {
		LinkValidator linkValidator = new LinkValidator(2,
				MediaType.valueOf("image/*"));
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element overlay = (Element) targetElements.item(i);
			Node icon = overlay.getElementsByTagNameNS(KML2.NS_NAME, "Icon")
					.item(0);
			Assert.assertNotNull(icon, ErrorMessage.format(
					ErrorMessageKeys.MISSING_INFOSET_ITEM, "kml:Icon",
					XMLUtils.buildXPointer(overlay)));
			Assert.assertTrue(linkValidator.isValid(icon),
					linkValidator.getErrorMessages());
		}
	}

}
