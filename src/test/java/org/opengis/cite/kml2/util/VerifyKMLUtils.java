package org.opengis.cite.kml2.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipException;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.cite.kml2.KML2;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the KMLUtils class.
 */
public class VerifyKMLUtils {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyKMLUtils() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@Test
	public void parseKMLFromTextFile() throws URISyntaxException, IOException,
			SAXException {
		thrown.expect(ZipException.class);
		URL url = this.getClass().getResource("/Jabberwocky.txt");
		File file = new File(url.toURI());
		Document doc = KMLUtils.parseKMLDocument(file);
		assertNull(doc);
	}

	@Test
	public void parseKMLDocument() throws URISyntaxException, IOException,
			SAXException {
		URL url = this.getClass().getResource("/kml23/Placemark-001.xml");
		File file = new File(url.toURI());
		Document doc = KMLUtils.parseKMLDocument(file);
		assertNotNull(doc);
	}

	@Test
	public void parseKMZArchive() throws URISyntaxException, IOException,
			SAXException {
		URL url = this.getClass().getResource("/kml22/small_world.kmz");
		File file = new File(url.toURI());
		Document doc = KMLUtils.parseKMLDocument(file);
		assertNotNull(doc);
	}

	@Test
	public void findSharedStyle() throws URISyntaxException {
		URL url = this.getClass().getResource("/kml22/SharedStyle.xml");
		File file = new File(url.toURI());
		Set<String> styleIdSet = KMLUtils.findElementIdentifiers(
				new StreamSource(file),
				"//kml:Document/kml:Style | //kml:Document/kml:StyleMap");
		assertEquals("Unexpected number of shared styles", 1, styleIdSet.size());
		assertTrue("Expected set to contain 'defaultStyles'",
				styleIdSet.contains("defaultStyles"));
	}

	@Test
	public void getDeclaredFieldsInTrailHeadType() throws URISyntaxException,
			SaxonApiException {
		URL url = this.getClass().getResource("/schemas/Schema-001.xml");
		File file = new File(url.toURI());
		XdmValue value = XMLUtils.evaluateXPath2(new StreamSource(file),
				"//kml:Schema[@id eq 'TrailHeadType']",
				Collections.singletonMap(KML2.NS_NAME, "kml"));
		Map<String, ItemType> fields = KMLUtils
				.getDeclaredFields((XdmNode) value.itemAt(0));
		assertEquals("Unexpected number of fields", 4, fields.size());
	}
}
