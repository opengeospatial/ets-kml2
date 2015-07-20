package org.opengis.cite.kml2.c1;

import javax.xml.xpath.XPathExpressionException;

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
import org.w3c.dom.NodeList;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Implements tests that apply to kml:Polygon elements. The relevant test cases
 * from the abstract test suite are listed below:
 * <ul>
 * <li>ATC-103: Valid geometry coordinates</li>
 * <li>ATC-117: Polygon boundary elements</li>
 * </ul>
 * 
 * @see "OGC 14-068r1: OGC KML 2.3 - Abstract Test Suite, Conformance Level 1"
 */
public class PolygonTests extends CommonFixture {

	private CoordinatesValidator coordsValidator;

	public PolygonTests() {
		this.coordsValidator = new CoordinatesValidator();
	}

	/**
	 * Finds kml:Polygon elements in the KML document that do not appear in an
	 * update context. If none are found, all test methods defined in the class
	 * are skipped.
	 */
	@BeforeClass
	public void findPolygonElements() {
		findTargetElements("Polygon");
	}

	/**
	 * [Test] Verifies that a kml:Polygon element has valid coordinates and is
	 * topologically correct. All boundary elements must be a valid LinearRing
	 * (see ATC-116). Each interior boundary, if present, must define a "hole"
	 * in the Polygon such that it lies within the exterior boundary.
	 */
	@Test(description = "ATC-103, ATC-117")
	public void validPolygonBoundary() {
		JTSGeometryBuilder geomBuilder = new JTSGeometryBuilder();
		Polygon crsPolygon = geomBuilder.buildPolygon(new Envelope(-180, 180,
				-90, 90));
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element polygon = (Element) targetElements.item(i);
			NodeList outerRing = null;
			try {
				outerRing = XMLUtils.evaluateXPath(polygon,
						"kml:outerBoundaryIs/kml:LinearRing", null);
			} catch (XPathExpressionException e) { // expression ok
			}
			Assert.assertTrue(outerRing.getLength() == 1, ErrorMessage.format(
					ErrorMessageKeys.POLYGON_BOUNDARY,
					"Missing outer boundary", XMLUtils.buildXPointer(polygon)));
			Assert.assertTrue(coordsValidator.isValid(outerRing.item(0)),
					coordsValidator.getErrorMessages());
			Polygon jtsPolygon = null;
			try {
				jtsPolygon = geomBuilder.buildPolygon(polygon);
			} catch (IllegalArgumentException ex) {
				throw new AssertionError(ErrorMessage.format(
						ErrorMessageKeys.POLYGON_BOUNDARY, ex.getMessage(),
						XMLUtils.buildXPointer(polygon)));
			}
			Assert.assertTrue(crsPolygon.covers(jtsPolygon), ErrorMessage
					.format(ErrorMessageKeys.OUTSIDE_CRS, jtsPolygon.toText()));
		}
	}

	/**
	 * [Test] Verifies that a kml:Polygon element has a valid altitudeMode value
	 * as determined by the values of its kml:extrude and kml:tessellate
	 * elements.
	 */
	@Test(description = "ATC-112, ATC-113")
	public void validAltitudeMode() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element polygon = (Element) targetElements.item(i);
			ETSAssert.assertValidAltitudeMode(polygon);
		}
	}

}
