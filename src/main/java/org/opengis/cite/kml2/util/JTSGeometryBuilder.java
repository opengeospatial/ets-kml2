package org.opengis.cite.kml2.util;

import java.util.ArrayList;
import java.util.List;

import org.opengis.cite.kml2.KML2;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
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
	 * @return A Point.
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
	 * Builds a LineString geometry from a kml:LineString element.
	 * 
	 * @param line
	 *            An Element node (kml:LineString).
	 * @return A LineString.
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
	 * @return A LinearRing.
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
	 * Builds a Polygon from the given envelope.
	 * 
	 * @param env
	 *            An envelope defining some spatial extent.
	 * @return A rectangular polygon that covers the same extent as the
	 *         envelope.
	 */
	public Polygon buildPolygon(Envelope env) {
		return (Polygon) this.geomFactory.toGeometry(env);
	}

	/**
	 * Creates a Coordinate sequence from the content of a kml:coordinates
	 * element.
	 * 
	 * @param coords
	 *            An Element node (kml:coordinates).
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
