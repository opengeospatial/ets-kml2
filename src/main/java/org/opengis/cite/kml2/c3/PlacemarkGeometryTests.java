package org.opengis.cite.kml2.c3;

import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.JTSGeometryBuilder;
import org.opengis.cite.kml2.util.XMLUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Checks CL3 constraints that apply to a KML geometry elements (that substitute
 * for kml:AbstractGeometryGroup).
 * 
 * @see "OGC 14-068r2, OGC KML 2.3 - Abstract Test Suite: Conformance Level 3"
 */
public class PlacemarkGeometryTests extends CommonFixture {

	JTSGeometryBuilder geomBuilder = new JTSGeometryBuilder();

	public PlacemarkGeometryTests() {
		super();
		this.geomBuilder = new JTSGeometryBuilder();
	}

	/**
	 * Finds kml:Placemark elements in the KML document that do not appear in an
	 * update context. If none are found, all test methods defined in the class
	 * are skipped.
	 */
	@BeforeClass
	public void findPlacemarks() {
		findTargetElements("Placemark");
	}

	/**
	 * [Test] Verifies that the rings comprising the boundary of a kml:Polygon
	 * geometry satisfy the constraints listed below.
	 * <ol>
	 * <li>no two rings cross (but they may intersect at a single point)</li>
	 * <li>the (exterior) coordinates are specified in a counterclockwise order</li>
	 * </ol>
	 * 
	 * <p>
	 * The surface of a polygon is oriented such that the interior is to the
	 * left of a boundary curve. This means that the exterior boundary of the
	 * surface runs counterclockwise when viewed from the side of the surface
	 * indicated by the upward normal (the "top" of the surface); interior
	 * boundaries are clockwise.
	 * </p>
	 */
	@Test(description = "ATC-301")
	public void polygonBoundaryOrientation() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element place = (Element) targetElements.item(i);
			Node kmlPolygon = place.getElementsByTagNameNS(KML2.NS_NAME,
					"Polygon").item(0);
			if (null == kmlPolygon) {
				continue;
			}
			Node outerRing;
			try {
				outerRing = XMLUtils.evaluateXPath(kmlPolygon,
						"kml:outerBoundaryIs/kml:LinearRing", null).item(0);
				Assert.assertNotNull(outerRing, "Missing ...");
			} catch (XPathExpressionException e) {
			}
			Element polygonElem = (Element) kmlPolygon;
			Polygon polygon = geomBuilder.buildPolygon(polygonElem);
			Coordinate[] exteriorCoords = polygon.getExteriorRing()
					.getCoordinates();
			Assert.assertTrue(CGAlgorithms.isCCW(exteriorCoords), ErrorMessage
					.format(ErrorMessageKeys.EXT_BOUNDARY_ORIENT,
							XMLUtils.buildXPointer(polygonElem)));
		}
	}
}
