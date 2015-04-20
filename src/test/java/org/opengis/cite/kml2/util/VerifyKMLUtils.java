package org.opengis.cite.kml2.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.ZipException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
}
