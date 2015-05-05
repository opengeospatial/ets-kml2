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
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Implements tests that apply to kml:LineString elements. The relevant test
 * cases from the abstract test suite are listed below:
 * <ul>
 * <li>ATC-103: Valid geometry coordinates</li>
 * <li>ATC-115: LineString coordinates</li>
 * </ul>
 * 
 * @see "OGC 14-068r1: OGC KML 2.3 - Abstract Test Suite, Conformance Level 1"
 */
public class LineStringTests extends CommonFixture {

	private CoordinatesValidator coordsValidator;

	public LineStringTests() {
		this.coordsValidator = new CoordinatesValidator();
	}

	/**
	 * Finds kml:LineString elements in the KML document that do not appear in
	 * an update context. If none are found, all test methods defined in the
	 * class are skipped.
	 */
	@BeforeClass
	public void findLineStringElements() {
		findTargetElements("LineString");
	}

	/**
	 * [Test] Verifies that a kml:LineString element has valid coordinates. It
	 * must contain two or more coordinate tuple in the default CRS.
	 */
	@Test(description = "ATC-103, ATC-115")
	public void validLineStringCoordinates() {
		JTSGeometryBuilder geomBuilder = new JTSGeometryBuilder();
		Polygon crsPolygon = geomBuilder.buildPolygon(new Envelope(-180, 180,
				-90, 90));
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element line = (Element) targetElements.item(i);
			Assert.assertTrue(coordsValidator.isValid(line),
					coordsValidator.getErrors());
			LineString jtsLine = geomBuilder.buildLineString(line);
			Assert.assertTrue(
					crsPolygon.covers(jtsLine),
					ErrorMessage.format(ErrorMessageKeys.OUTSIDE_CRS,
							jtsLine.toText()));
		}
	}

}
