package org.opengis.cite.kml2.c1;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.JTSGeometryBuilder;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.kml2.validation.CoordinatesValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Implements tests that apply to kml:LatLonQuad elements. The relevant test
 * cases from the abstract test suite are listed below:
 * <ul>
 * <li>ATC-103: Valid geometry coordinates</li>
 * <li>ATC-149: LatLonQuad coordinates</li>
 * </ul>
 * 
 * The element may appear in a GroundOverlay feature, but is prohibited in
 * kml:Region.
 * 
 * @see "OGC 12-007r1, 11.4:  11.4	kml:LatLonQuad"
 * @see "OGC 14-068r1, OGC KML 2.3 - Abstract Test Suite: Conformance Level 1"
 */
public class LatLonQuadTests extends CommonFixture {

	private CoordinatesValidator coordsValidator;

	public LatLonQuadTests() {
		this.coordsValidator = new CoordinatesValidator();
	}

	/**
	 * Finds kml:LatLonQuad elements in the KML document that do not appear in
	 * an update context. If none are found, all test methods defined in the
	 * class are skipped.
	 */
	@BeforeClass
	public void findLatLonQuadElements() {
		findTargetElements("LatLonQuad");
	}

	/**
	 * [Test] Verifies that a kml:LatLonQuad element has valid coordinates and
	 * satisfies the following additional constraints:
	 * <ol>
	 * <li>the four coordinate tuples are specified in counter-clockwise order
	 * (the interior is to the left of the boundary curve), with the first
	 * coordinate corresponding to the lower-left corner of the overlayed image;
	 * </li>
	 * <li>the quadrilateral is convex (every interior angle <= 180 degrees).</li>
	 * </ol>
	 */
	@Test(description = "ATC-103, ATC-149")
	public void validLatLonQuadCoordinates() {
		JTSGeometryBuilder jtsBuilder = new JTSGeometryBuilder();
		Polygon crsPolygon = jtsBuilder.buildPolygon(new Envelope(-180, 180,
				-90, 90));
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element quad = (Element) targetElements.item(i);
			Assert.assertTrue(coordsValidator.isValid(quad),
					coordsValidator.getErrors());
			Polygon jtsPolygon = null;
			try {
				Node coords = quad.getElementsByTagNameNS(KML2.NS_NAME,
						"coordinates").item(0);
				jtsPolygon = jtsBuilder.buildPolygonFromCoordinates(coords);
			} catch (IllegalArgumentException ex) {
				throw new AssertionError(ErrorMessage.format(
						ErrorMessageKeys.POLYGON_BOUNDARY, ex.getMessage(),
						XMLUtils.buildXPointer(quad)));
			}
			Assert.assertTrue(crsPolygon.covers(jtsPolygon), ErrorMessage
					.format(ErrorMessageKeys.OUTSIDE_CRS, jtsPolygon.toText()));
			Assert.assertTrue(
					CGAlgorithms.isCCW(jtsPolygon.getCoordinates()),
					ErrorMessage.format(ErrorMessageKeys.RING_NOT_CCW,
							XMLUtils.buildXPointer(quad)));
			// if convex, should be topologically equivalent to convex hull
			Assert.assertTrue(jtsPolygon.convexHull().equalsTopo(jtsPolygon),
					ErrorMessage.format(ErrorMessageKeys.QUAD_NOT_CONVEX,
							XMLUtils.buildXPointer(quad)));
		}
	}

}
