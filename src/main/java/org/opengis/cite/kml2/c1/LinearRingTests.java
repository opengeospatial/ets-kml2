package org.opengis.cite.kml2.c1;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ETSAssert;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.util.JTSGeometryBuilder;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.kml2.validation.CoordinatesValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Implements tests that apply to kml:LinearRing elements. The relevant test
 * cases from the abstract test suite are listed below:
 * <ul>
 * <li>ATC-103: Valid geometry coordinates</li>
 * <li>ATC-116: LinearRing coordinates</li>
 * </ul>
 * 
 * @see "OGC 14-068r1: OGC KML 2.3 - Abstract Test Suite, Conformance Level 1"
 */
public class LinearRingTests extends CommonFixture {

	private CoordinatesValidator coordsValidator;

	public LinearRingTests() {
		this.coordsValidator = new CoordinatesValidator();
	}

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
	 * [Test] Verifies that a kml:LinearRing element has valid coordinates. It
	 * must contain a sequence of four or more coordinate tuples in the default
	 * CRS. Furthermore, the first and last control points must be coincident
	 * (i.e. the ring is explicitly closed).
	 */
	@Test(description = "ATC-103, ATC-116")
	public void validLinearRingCoordinates() {
		JTSGeometryBuilder geomBuilder = new JTSGeometryBuilder();
		Polygon crsPolygon = geomBuilder.buildPolygon(new Envelope(-180, 180,
				-90, 90));
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element ring = (Element) targetElements.item(i);
			Assert.assertTrue(coordsValidator.isValid(ring),
					coordsValidator.getErrors());
			LinearRing jtsRing = null;
			try {
				jtsRing = geomBuilder.buildLinearRing(ring);
			} catch (IllegalArgumentException ex) {
				throw new AssertionError(ErrorMessage.format(
						ErrorMessageKeys.OPEN_RING,
						XMLUtils.buildXPointer(ring)));
			}
			Assert.assertTrue(
					crsPolygon.covers(jtsRing),
					ErrorMessage.format(ErrorMessageKeys.OUTSIDE_CRS,
							jtsRing.toText()));
		}
	}

	/**
	 * [Test] Verifies that a kml:LinearRing element has a valid altitudeMode
	 * value as determined by the values of its kml:extrude and kml:tessellate
	 * elements.
	 */
	@Test(description = "ATC-112, ATC-113")
	public void validAltitudeMode() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element ring = (Element) targetElements.item(i);
			ETSAssert.assertValidAltitudeMode(ring);
		}
	}

}
