package org.opengis.cite.kml2.c1;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.util.JTSGeometryBuilder;
import org.opengis.cite.kml2.validation.CoordinatesValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Envelope;
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

	public PointTests() {
		this.coordsValidator = new CoordinatesValidator();
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
	 * [Test] Verifies that a kml:Point element has valid coordinates.
	 */
	@Test(description = "ATC-103")
	public void validPointCoordinates() {
		JTSGeometryBuilder geomBuilder = new JTSGeometryBuilder();
		Polygon crsPolygon = geomBuilder.buildPolygon(new Envelope(-180, 180,
				-90, 90));
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element point = (Element) targetElements.item(i);
			Assert.assertTrue(coordsValidator.isValid(point),
					coordsValidator.getErrors());
			Point jtsPoint = geomBuilder.buildPoint(point);
			Assert.assertTrue(
					crsPolygon.covers(jtsPoint),
					ErrorMessage.format(ErrorMessageKeys.OUTSIDE_CRS,
							jtsPoint.toText()));
		}
	}

}
