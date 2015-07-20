package org.opengis.cite.kml2.c1;

import org.opengis.cite.kml2.CommonFeatureTests;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.validation.TrackValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implements tests that apply to the kml:Placemark element. A Placemark is a
 * feature that may have an associated geometry element (that substitutes for
 * kml:AbstractGeometryGroup).
 * 
 * @see "OGC 12-007r2: OGC KML 2.3, 9.14"
 * @see "OGC 14-068r2: OGC KML 2.3 - Abstract Test Suite, Conformance Level 1"
 */
public class PlacemarkTests extends CommonFeatureTests {

	private TrackValidator trackValidator;

	public PlacemarkTests() {
		this.trackValidator = new TrackValidator();
	}

	/**
	 * Finds kml:Placemark elements in the KML resource that do not appear in an
	 * update context. If none are found, all test methods defined in the class
	 * are skipped.
	 */
	@BeforeClass
	public void findPlacemarkElements() {
		findTargetElements("Placemark");
	}

	@Test(description = "ATC-146, ATC-147, ATC-148")
	public void validTrack() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element place = (Element) targetElements.item(i);
			Node track = place.getElementsByTagNameNS(KML2.NS_NAME, "Track")
					.item(0);
			if (null == track) {
				continue;
			}
			Assert.assertTrue(trackValidator.isValid(track),
					trackValidator.getErrorMessages());
		}
	}

}
