package org.opengis.cite.kml2.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.transform.Source;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * 
 * Provides various utility methods for reading or manipulating KML and KMZ
 * resources.
 */
public class KMLUtils {

	/**
	 * Parses the content of the given file as a KML resource and returns the
	 * resulting DOM document node.
	 * 
	 * @param file
	 *            A file containing a KML resource (KML or KMZ).
	 * @return A Document object representing a KML document (the root element
	 *         is kml:kml), or null if one cannot be found.
	 * @throws IOException
	 *             If the file cannot be read for some reason (e.g. it doesn't
	 *             exist).
	 * @throws SAXException
	 *             If the file does not contain well-formed XML.
	 */
	public static Document parseKMLDocument(File file) throws IOException,
			SAXException {
		Document kmlDoc = null;
		try (FileInputStream fileStream = new FileInputStream(file)) {
			if (XMLUtils.isXML(fileStream)) {
				kmlDoc = (Document) URIUtils.parseURI(file.toURI());
			} else {
				kmlDoc = parseKMLDocumentInArchive(file);
			}
		}
		return kmlDoc;
	}

	/**
	 * Reads the given KMZ archive file and parses the first root-level KML
	 * document found within it. The main KML document is conventionally named
	 * <em>doc.kml</em> but this is not required; the {@code .kml} extension is
	 * expected, however.
	 * 
	 * @param file
	 *            A File object that presumably represents a KMZ file (ZIP
	 *            archive).
	 * @return A KML document, or {@code null} if a root-level KML file could
	 *         not be found in the archive.
	 * @throws IOException
	 *             The file is not a valid ZIP archive or some other I/O error
	 *             occurred.
	 * @throws SAXException
	 *             If a KML document was found but it is not well-formed.
	 * 
	 * 
	 * @see "OGC 12-007r1, Annex C: KMZ Files (Normative)"
	 * @see <a
	 *      href="https://developers.google.com/kml/documentation/kmzarchives">What
	 *      is a KMZ File?</a>
	 */
	public static Document parseKMLDocumentInArchive(File file)
			throws IOException, SAXException {
		if (!file.exists()) {
			throw new IllegalArgumentException("File does not exist: "
					+ file.getAbsolutePath());
		}
		Document mainKMLDoc = null;
		try (ZipFile zipFile = new ZipFile(file)) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				File destFile = new File(file.getParent(), entry.getName());
				if (entry.isDirectory()) {
					destFile.mkdirs();
					continue;
				}
				InputStream input = zipFile.getInputStream(entry);
				OutputStream output = new FileOutputStream(destFile);
				int nBytes = IOUtils.copy(input, output);
				IOUtils.closeQuietly(input);
				IOUtils.closeQuietly(output);
				if ((null == mainKMLDoc) && destFile.getName().endsWith(".kml")) {
					mainKMLDoc = (Document) URIUtils.parseURI(destFile.toURI());
				}
				if (TestSuiteLogger.isLoggable(Level.FINER)) {
					TestSuiteLogger.log(Level.FINER, String.format(
							"Extracted %d bytes to %s", nBytes,
							destFile.toURI()));
				}
			}
		}
		return mainKMLDoc;
	}

	/**
	 * Finds shared styles that occur in a KML document. A shared style is any
	 * element that may substitute for kml:AbstractStyleSelectorGroup
	 * (kml:Style, kml:StyleMap) that satisfies all of the following conditions:
	 * <ol>
	 * <li>its parent element is kml:Document</li>
	 * <li>it has a non-empty 'id' attribute value</li>
	 * </ol>
	 * 
	 * @param kmlSource
	 *            A Source for reading a KML document.
	 * @return A set (possibly empty) of identifiers for shared styles defined
	 *         in the source resource.
	 * 
	 * @see "OGC KML 2.3, 6.4: Shared Styles"
	 */
	public static Set<String> findSharedStyles(Source kmlSource) {
		XdmValue results = null;
		Set<String> styleIdSet = new HashSet<String>();
		String expr = "//kml:Document/kml:Style/@id | //kml:Document/kml:StyleMap/@id";
		try {
			results = XMLUtils.evaluateXPath2(kmlSource, expr, null);
		} catch (SaxonApiException e) {
			Logger.getLogger(KMLUtils.class.getName()).log(Level.WARNING,
					"Failed to evaluate XPath expression: " + expr, e);
			return styleIdSet;
		}
		for (XdmItem item : results) {
			String id = item.getStringValue().trim();
			if (id.isEmpty()) {
				continue;
			}
			styleIdSet.add(id);
		}
		return styleIdSet;
	}

}
