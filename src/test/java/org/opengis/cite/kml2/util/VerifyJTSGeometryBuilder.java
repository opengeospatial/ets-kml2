package org.opengis.cite.kml2.util;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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

}
