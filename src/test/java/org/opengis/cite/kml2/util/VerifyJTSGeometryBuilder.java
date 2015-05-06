package org.opengis.cite.kml2.util;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.kml2.KML2;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Verifies the behavior of the JTSGeometryBuilder class.
 */
public class VerifyJTSGeometryBuilder {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private static DocumentBuilder docBuilder;

	public VerifyJTSGeometryBuilder() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		docBuilder = dbf.newDocumentBuilder();
	}

	@Test
	public void buildPolygonNoHoles() throws SAXException, IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/geom/Polygon.xml"));
		JTSGeometryBuilder iut = new JTSGeometryBuilder();
		Polygon polygon = iut.buildPolygon(doc.getDocumentElement());
		assertNotNull(polygon);
		assertEquals("Unexpected number of interior rings.", 0,
				polygon.getNumInteriorRing());
	}

	@Test
	public void buildPolygonWithHole() throws SAXException, IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/geom/PolygonWithHole.xml"));
		JTSGeometryBuilder iut = new JTSGeometryBuilder();
		Polygon polygon = iut.buildPolygon(doc.getDocumentElement());
		assertNotNull(polygon);
		assertEquals("Unexpected number of interior rings.", 1,
				polygon.getNumInteriorRing());
	}

	@Test
	public void buildPolygonWithOpenExterior() throws SAXException, IOException {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Points of LinearRing do not form a closed linestring");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/geom/PolygonNotClosed.xml"));
		JTSGeometryBuilder iut = new JTSGeometryBuilder();
		Polygon polygon = iut.buildPolygon(doc.getDocumentElement());
		assertNull(polygon);
	}

	@Test
	public void buildPolygonWithHoleOutside() throws SAXException, IOException {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Inner boundary [1] not inside outer boundary");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/geom/PolygonWithHoleOutside.xml"));
		JTSGeometryBuilder iut = new JTSGeometryBuilder();
		Polygon polygon = iut.buildPolygon(doc.getDocumentElement());
		assertNull(polygon);
	}

	@Test
	public void buildPolygonFromConvexLatLonQuad() throws SAXException,
			IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/geom/LatLonQuad.xml"));
		Node coords = doc.getDocumentElement()
				.getElementsByTagNameNS(KML2.NS_NAME, "coordinates").item(0);
		JTSGeometryBuilder iut = new JTSGeometryBuilder();
		Polygon polygon = iut.buildPolygonFromCoordinates(coords);
		assertEquals("Unexpected number of vertices.", 5,
				polygon.getNumPoints());
		Coordinate expected = new Coordinate(81.601884, 44.160723);
		assertTrue("Expected first coord: " + expected.toString(), polygon
				.getExteriorRing().getCoordinateN(0).equals2D(expected));
	}

	@Test
	public void buildPolygonFromConcaveLatLonQuad() throws SAXException,
			IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
				"/geom/LatLonQuad-NonConvex.xml"));
		Node coords = doc.getDocumentElement()
				.getElementsByTagNameNS(KML2.NS_NAME, "coordinates").item(0);
		JTSGeometryBuilder iut = new JTSGeometryBuilder();
		Polygon polygon = iut.buildPolygonFromCoordinates(coords);
		assertEquals("Unexpected number of vertices.", 5,
				polygon.getNumPoints());
		Coordinate expected = new Coordinate(-123, 50.0);
		assertTrue("Expected first coord: " + expected.toString(), polygon
				.getExteriorRing().getCoordinateN(0).equals2D(expected));
	}
}
