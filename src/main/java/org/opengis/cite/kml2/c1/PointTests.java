package org.opengis.cite.kml2.c1;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.validation.CoordinatesValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Implements tests that apply to kml:Point elements. The relevant test cases
 * from the abstract test suite are listed below:
 * <ul>
 * <li>ATC-103: Valid geometry coordinates</li>
 * </ul>
 * 
 * @see "OGC 14-068r1: OGC KML 2.3 - Abstract Test Suite, Conformance Level 1"
 */
public class PointTests extends CommonFixture {

	private CoordinatesValidator coordsValidator;
	private GeometryFactory geomFactory;
	private Polygon crsAreaOfUse;

	public PointTests() {
		this.coordsValidator = new CoordinatesValidator();
		this.geomFactory = new GeometryFactory();
		Envelope crsEnv = new Envelope(-180, 180, -90, 90);
		this.crsAreaOfUse = (Polygon) geomFactory.toGeometry(crsEnv);
	}

	/**
	 * Finds kml:Point elements in the KML document that do not appear in an
	 * update context. If none are found, all test methods defined in the class
	 * are skipped.
	 */
	@BeforeClass
	public void findPointElements() {
		findTargetElements("Point");
	}

	/**
	 * [Test] Verifies that a kml:Point element is valid.
	 */
	@Test(description = "ATC-103")
	public void validPoint() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element point = (Element) targetElements.item(i);
			Assert.assertTrue(coordsValidator.isValid(point),
					coordsValidator.getErrors());
			Point jtsPoint = createPoint(point);
			Assert.assertTrue(
					crsAreaOfUse.covers(jtsPoint),
					ErrorMessage.format(ErrorMessageKeys.OUTSIDE_CRS,
							jtsPoint.toText()));
		}
	}

	Point createPoint(Element point) {
		if (!point.getLocalName().equals("Point")) {
			throw new IllegalArgumentException(
					"Element does not represent a Point.");
		}
		String[] tuple = point
				.getElementsByTagNameNS(KML2.NS_NAME, "coordinates").item(0)
				.getTextContent().trim().split(",");
		double alt = (tuple.length > 2) ? Double.parseDouble(tuple[2]) : 0;
		Coordinate coord = new Coordinate(Double.parseDouble(tuple[0]),
				Double.parseDouble(tuple[1]), alt);
		return this.geomFactory.createPoint(coord);
	}
}
