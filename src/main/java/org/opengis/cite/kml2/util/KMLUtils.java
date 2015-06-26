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

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import org.apache.commons.io.IOUtils;
import org.opengis.cite.kml2.AltitudeMode;
import org.opengis.cite.kml2.KML2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
	 * Finds KML elements selected by the given XPath expression and returns
	 * their identifiers. This method can be used to find custom schemas and
	 * shared styles that occur in a KML document and may be referenced by
	 * identifier.
	 * <p>
	 * A shared style is any element that may substitute for
	 * kml:AbstractStyleSelectorGroup (kml:Style, kml:StyleMap) that satisfies
	 * all of the following conditions:
	 * </p>
	 * <ol>
	 * <li>its parent element is kml:Document</li>
	 * <li>it has a non-empty 'id' attribute value</li>
	 * </ol>
	 * 
	 * <p>
	 * A custom schema (kml:Schema) may be defined in order to add user-defined
	 * data that occurs within a child kml:ExtendedData element of a KML
	 * feature.
	 * </p>
	 * 
	 * @param kmlSource
	 *            A Source for reading a KML document.
	 * @param xpath
	 *            An XPath (2.0) expression that selects the KML objects of
	 *            interest.
	 * @return A set (possibly empty) of identifiers for shared resources
	 *         defined in the source.
	 * 
	 * @see "OGC KML 2.3, 6.4: Shared Styles"
	 * @see "OGC KML 2.3, 9.10: Schema"
	 */
	public static Set<String> findElementIdentifiers(Source kmlSource,
			String xpath) {
		XdmValue results = null;
		Set<String> idSet = new HashSet<String>();
		try {
			results = XMLUtils.evaluateXPath2(kmlSource, xpath, null);
		} catch (SaxonApiException e) {
			Logger.getLogger(KMLUtils.class.getName()).log(Level.WARNING,
					"Failed to evaluate XPath expression: " + xpath, e);
			return idSet;
		}
		for (XdmItem item : results) {
			XdmNode node = (XdmNode) item;
			String id = node.getAttributeValue(new QName("id"));
			if (null == id || id.isEmpty()) {
				continue;
			}
			idSet.add(id);
		}
		return idSet;
	}

	/**
	 * Gets the altitude mode for the given KML element. The schema component
	 * kml:AltitudeModeGroup includes the elements kml:altitudeMode and
	 * kml:seaFloorAltitudeMode, for which the following values are declared:
	 * <ul>
	 * <li>clampToGround (default)</li>
	 * <li>relativeToGround</li>
	 * <li>absolute</li>
	 * <li>clampToSeaFloor</li>
	 * <li>relativeToSeaFloor</li>
	 * </ul>
	 * 
	 * @param element
	 *            A KML element.
	 * @return The AltitudeMode that applies this element, or the default if not
	 *         explicitly set.
	 */
	public static AltitudeMode getAltitudeMode(Element element) {
		AltitudeMode altMode = AltitudeMode.CLAMP_TO_GROUND;
		Node altitudeMode = element.getElementsByTagNameNS(KML2.NS_NAME,
				"altitudeMode").item(0);
		Node seafloorAltitudeMode = element.getElementsByTagNameNS(
				KML2.NS_NAME, "seaFloorAltitudeMode").item(0);
		if (null != seafloorAltitudeMode) {
			altMode = AltitudeMode.fromString(seafloorAltitudeMode
					.getTextContent().trim());
		} else if (null != altitudeMode) {
			altMode = AltitudeMode.fromString(altitudeMode.getTextContent()
					.trim());
		}
		return altMode;
	}

}
