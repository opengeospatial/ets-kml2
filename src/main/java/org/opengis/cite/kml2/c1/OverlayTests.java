package org.opengis.cite.kml2.c1;

import java.net.URL;

import javax.ws.rs.core.MediaType;
import javax.xml.transform.dom.DOMSource;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ETSAssert;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.kml2.validation.GeoExtentValidator;
import org.opengis.cite.kml2.validation.LinkValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
	private GeoExtentValidator geoExtentValidator;

	public OverlayTests() {
		this.linkValidator = new LinkValidator(MediaType.valueOf("image/*"));
		this.geoExtentValidator = new GeoExtentValidator();
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
	@Test(description = "ATC-110, ATC-118")
	public void overlayIcon() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element overlay = (Element) targetElements.item(i);
			Node icon = overlay.getElementsByTagNameNS(KML2.NS_NAME, "Icon")
					.item(0);
			if (null != icon) {
				Assert.assertTrue(linkValidator.isValid(icon),
						linkValidator.getErrorMessages());
			}
		}
	}

	/**
	 * [Test] Verifies that a GroundOverlay element has a valid geographic
	 * extent (kml:LatLonBox or kml:LatLonQuad).
	 * 
	 * @see "[OGC 12-007r2] OGC KML 2.3, 6.3.4: kml:GroundOverlay and kml:Region"
	 * @see "[OGC 12-007r2] OGC KML 2.3, 11.2: kml:GroundOverlay"
	 */
	@Test(description = "ATC-111")
	public void groundOverlayExtent() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element overlay = (Element) targetElements.item(i);
			if (!overlay.getLocalName().equals("GroundOverlay")) {
				continue;
			}
			Node extent = overlay.getElementsByTagNameNS(KML2.NS_NAME,
					"LatLonBox").item(0);
			if (null == extent) {
				extent = overlay.getElementsByTagNameNS(KML2.NS_NAME,
						"LatLonQuad").item(0);
			}
			Assert.assertNotNull(extent, ErrorMessage.format(
					ErrorMessageKeys.MISSING_INFOSET_ITEM,
					"kml:LatLonBox or kml:LatLonQuad",
					XMLUtils.buildXPointer(overlay)));
			Assert.assertTrue(geoExtentValidator.validGeoExtent(extent),
					geoExtentValidator.getErrors());
		}
	}

	/**
	 * [Test] Checks various Schematron constraints that apply to specific types
	 * of overlay features:
	 * <ul>
	 * <li>ATC-119: kml:PhotoOverlay has a valid field of view</li>
	 * <li>ATC-132: kml:GroundOverlay with an kml:altitudeMode value of
	 * "absolute" has a kml:altitude element</li>
	 * <li>ATC-134: kml:PhotoOverlay contains all of the following child
	 * elements: kml:Icon, kml:ViewVolume, kml:Point, and kml:Camera</li>
	 * </ul>
	 */
	@Test(description = "ATC-119, ATC-132, ATC-134")
	public void checkOverlayConstraints() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element overlay = (Element) targetElements.item(i);
			URL schRef = this.getClass().getResource(
					"/org/opengis/cite/kml2/sch/kml-overlay.sch");
			ETSAssert.assertSchematronValid(schRef, new DOMSource(overlay));
		}
	}
}
