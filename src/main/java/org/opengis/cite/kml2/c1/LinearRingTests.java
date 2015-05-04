package org.opengis.cite.kml2.c1;

import java.util.ArrayList;
import java.util.List;

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
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Implements tests that apply to kml:LinearRing elements. The relevant test
 * cases from the abstract test suite are listed below:
 * <ul>
 * <li>ATC-103: Valid geometry coordinates</li>
 * </ul>
 * 
 * @see "OGC 14-068r1: OGC KML 2.3 - Abstract Test Suite, Conformance Level 1"
 */
public class LinearRingTests extends CommonFixture {

	private CoordinatesValidator coordsValidator;
	private GeometryFactory geomFactory;
	private Polygon crsAreaOfUse;

	public LinearRingTests() {
		this.coordsValidator = new CoordinatesValidator();
		this.geomFactory = new GeometryFactory();
		Envelope crsEnv = new Envelope(-180, 180, -90, 90);
		this.crsAreaOfUse = (Polygon) geomFactory.toGeometry(crsEnv);
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
	 * [Test] Verifies that a kml:LinearRing element is valid. The sequence of
	 * coordinate tuples must constitute a closed ring. That is, the first and
	 * last positions are coincident.
	 */
	@Test(description = "ATC-103")
	public void validLinearRing() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element ring = (Element) targetElements.item(i);
			Assert.assertTrue(coordsValidator.isValid(ring),
					coordsValidator.getErrors());
			LineString jtsLine = createLineString(ring);
			Assert.assertTrue(
					crsAreaOfUse.covers(jtsLine),
					ErrorMessage.format(ErrorMessageKeys.OUTSIDE_CRS,
							jtsLine.toText()));
			Assert.assertTrue(
					jtsLine.isRing(),
					ErrorMessage.format(ErrorMessageKeys.OPEN_RING,
							jtsLine.toText()));
		}
	}

	LineString createLineString(Element ring) {
		if (!ring.getLocalName().equals("LinearRing")) {
			throw new IllegalArgumentException(
					"Element does not represent a LinearRing.");
		}
		String[] tuples = ring
				.getElementsByTagNameNS(KML2.NS_NAME, "coordinates").item(0)
				.getTextContent().trim().split("\\s+");
		List<Coordinate> coordList = new ArrayList<>();
		for (String tuple : tuples) {
			String[] coordTuple = tuple.trim().split(",");
			double alt = (coordTuple.length > 2) ? Double
					.parseDouble(coordTuple[2]) : 0;
			Coordinate coord = new Coordinate(
					Double.parseDouble(coordTuple[0]),
					Double.parseDouble(coordTuple[1]), alt);
			coordList.add(coord);
		}
		return this.geomFactory.createLineString(coordList
				.toArray(new Coordinate[tuples.length]));
	}
}
