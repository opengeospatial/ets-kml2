package org.opengis.cite.kml2.c1;

import javax.ws.rs.core.MediaType;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.validation.LinkValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Implements tests that apply to overlay features (of type
 * kml:AbstractOverlayType). These include kml:GroundOverlay, kml:ScreenOverlay,
 * and kml:PhotoOverlay.
 * 
 * Outside of an update context the following constraints apply:
 * <ul>
 * <li>a kml:Icon element specifies the location of an image resource;</li>
 * </ul>
 * 
 * The relevant test cases from the abstract test suite are listed below:
 * <ul>
 * <li>ATC-110: Link referent</li>
 * </ul>
 * 
 * @see "OGC 12-007r1: OGC KML 2.3, 10.9.2"
 * @see "OGC 14-068r1: OGC KML 2.3 - Abstract Test Suite, Conformance Level 1"
 */
public class OverlayTests extends CommonFixture {

	private LinkValidator linkValidator;

	public OverlayTests() {
		this.linkValidator = new LinkValidator(MediaType.valueOf("image/*"));
	}

	/**
	 * Finds overlay elements (Ground, Screen, Photo) in the KML document that
	 * do not appear in an update context. If none are found, all test methods
	 * defined in the class are skipped.
	 */
	@BeforeClass
	public void findOverlayElements() {
		findTargetElements("GroundOverlay", "ScreenOverlay", "PhotoOverlay");
	}

	/**
	 * [Test] Verifies that an overlay element has a valid reference (kml:Icon)
	 * to an image resource. The target must exist and it must be some type of
	 * image. If no kml:Icon element occurs, a rectangle is drawn using the
	 * color and size defined by the ground or screen overlay.
	 * 
	 * @see "[OGC 12-007r1] OGC KML 2.3, 11.1: kml:AbstractOverlayGroup"
	 * @see "[OGC 12-007r1] OGC KML 2.3, 11.1.3.3: kml:Icon"
	 */
	@Test(description = "ATC-110")
	public void overlayIcon() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element overlay = (Element) targetElements.item(i);
			NodeList icon = overlay
					.getElementsByTagNameNS(KML2.NS_NAME, "Icon");
			if (icon.getLength() > 0) {
				Assert.assertTrue(linkValidator.isValid(icon.item(0)),
						linkValidator.getErrors());
			}
		}
	}

}
