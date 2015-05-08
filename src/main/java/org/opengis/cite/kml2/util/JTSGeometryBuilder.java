package org.opengis.cite.kml2.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.kml2.KML2;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Builds JTS geometry objects from KML representations.
 */
public class JTSGeometryBuilder {

	private GeometryFactory geomFactory;

	public JTSGeometryBuilder() {
		this.geomFactory = new GeometryFactory();
	}

	/**
	 * Builds a Point geometry from a kml:Point element.
	 * 
	 * @param point
	 *            An Element node (kml:Point).
	 * @return A JTS Point.
	 */
	public Point buildPoint(Element point) {
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

	/**
	 * Builds a Point geometry from a kml:Location element.
	 * 
	 * @param location
	 *            An Element node (kml:Location).
	 * @return A JTS Point.
	 */
	public Point buildPointFromLocation(Element location) {
		if (!location.getLocalName().equals("Location")) {
			throw new IllegalArgumentException(
					"Element does not represent a Location.");
		}
		NodeList nodes = location.getElementsByTagNameNS(KML2.NS_NAME,
				"longitude");
		double lon = (nodes.getLength() > 0) ? Double.parseDouble(nodes.item(0)
				.getTextContent()) : 0;
		nodes = location.getElementsByTagNameNS(KML2.NS_NAME, "latitude");
		double lat = (nodes.getLength() > 0) ? Double.parseDouble(nodes.item(0)
				.getTextContent()) : 0;
		nodes = location.getElementsByTagNameNS(KML2.NS_NAME, "altitude");
		double alt = (nodes.getLength() > 0) ? Double.parseDouble(nodes.item(0)
				.getTextContent()) : 0;
		Coordinate coord = new Coordinate(lon, lat, alt);
		return this.geomFactory.createPoint(coord);
	}

	/**
	 * Builds a LineString geometry from a kml:LineString element.
	 * 
	 * @param line
	 *            An Element node (kml:LineString).
	 * @return A JTS LineString.
	 */
	public LineString buildLineString(Element line) {
		if (!line.getLocalName().equals("LineString")) {
			throw new IllegalArgumentException(
					"Element does not represent a LineString.");
		}
		List<Coordinate> coordList = createCoordinateList(line
				.getElementsByTagNameNS(KML2.NS_NAME, "coordinates").item(0));
		return this.geomFactory.createLineString(coordList
				.toArray(new Coordinate[coordList.size()]));
	}

	/**
	 * Builds a LinearRing geometry from a kml:LinearRing element.
	 * 
	 * @param ring
	 *            An Element node (kml:LinearRing).
	 * @return A JTS LinearRing.
	 */
	public LinearRing buildLinearRing(Element ring) {
		if (!ring.getLocalName().equals("LinearRing")) {
			throw new IllegalArgumentException(
					"Element does not represent a LinearRing.");
		}
		List<Coordinate> coordList = createCoordinateList(ring
				.getElementsByTagNameNS(KML2.NS_NAME, "coordinates").item(0));
		return this.geomFactory.createLinearRing(coordList
				.toArray(new Coordinate[coordList.size()]));
	}

	/**
	 * Builds a Polygon geometry from a kml:Polygon element.
	 * 
	 * @param ring
	 *            An Element node (kml:Polygon).
	 * @return A JTS Polygon.
	 * 
	 * @throws IllegalArgumentException
	 *             If a boundary element (ring) is not closed or an inner
	 *             boundary is not inside the outer boundary.
	 */
	public Polygon buildPolygon(Element polygon) {
		if (!polygon.getLocalName().equals("Polygon")) {
			throw new IllegalArgumentException(
					"Element does not represent a Polygon.");
		}
		LinearRing outerRing = null;
		NodeList rings = null;
		try {
			rings = XMLUtils.evaluateXPath(polygon,
					"kml:outerBoundaryIs/kml:LinearRing", null);
			outerRing = buildLinearRing((Element) rings.item(0));
			rings = XMLUtils.evaluateXPath(polygon,
					"kml:innerBoundaryIs/kml:LinearRing", null);
		} catch (XPathExpressionException e) { // expressions ok
		}
		Geometry outerPolygon = geomFactory.createPolygon(outerRing);
		LinearRing[] innerRings = new LinearRing[rings.getLength()];
		for (int i = 0; i < rings.getLength(); i++) {
			LinearRing innerRing = buildLinearRing((Element) rings.item(i));
			if (!outerPolygon.contains(innerRing)) {
				throw new IllegalArgumentException(
						String.format(
								"Inner boundary [%d] not inside outer boundary.",
								i + 1));
			}
			innerRings[i] = innerRing;
		}
		return this.geomFactory.createPolygon(outerRing, innerRings);
	}

	/**
	 * Builds a Polygon from the given envelope.
	 * 
	 * @param env
	 *            A JTS envelope defining some spatial extent.
	 * @return A rectangular polygon that covers the same extent as the
	 *         envelope.
	 */
	public Polygon buildPolygon(Envelope env) {
		return (Polygon) this.geomFactory.toGeometry(env);
	}

	/**
	 * Builds a Polygon with an exterior boundary delimited by the given
	 * sequence of coordinates. The sequence will be closed if necessary such
	 * that the first and last vertices are coincident.
	 * 
	 * @param coords
	 *            A node containing a list of coordinates (kml:coordinates).
	 * @return A JTS Polygon (with no interior boundaries).
	 */
	public Polygon buildPolygonFromCoordinates(Node coords) {
		List<Coordinate> coordList = createCoordinateList(coords);
		Coordinate lastCord = coordList.get(coordList.size() - 1);
		if (!coordList.get(0).equals2D(lastCord)) {
			// make a ring (e.g. LatLonQuad)
			coordList.add(new Coordinate(coordList.get(0)));
		}
		Polygon polygon = this.geomFactory.createPolygon(coordList
				.toArray(new Coordinate[coordList.size()]));
		return polygon;
	}

	/**
	 * Creates a Coordinate sequence from the content of a kml:coordinates
	 * element.
	 * 
	 * @param coords
	 *            A node containing a coordinate sequence (kml:coordinates).
	 * @return A list containing one or more Coordinate objects.
	 */
	List<Coordinate> createCoordinateList(Node coords) {
		if (!coords.getLocalName().equals("coordinates")) {
			throw new IllegalArgumentException(
					"Node does not represent a list of coordinates.");
		}
		List<Coordinate> coordList = new ArrayList<>();
		String[] tuples = coords.getTextContent().trim().split("\\s+");
		for (String tuple : tuples) {
			String[] coordTuple = tuple.trim().split(",");
			double alt = (coordTuple.length > 2) ? Double
					.parseDouble(coordTuple[2]) : 0;
			Coordinate coord = new Coordinate(
					Double.parseDouble(coordTuple[0]),
					Double.parseDouble(coordTuple[1]), alt);
			coordList.add(coord);
		}
		return coordList;
	}
}
