package org.opengis.cite.kml2;

import java.net.URI;

/**
 * XML namespace names.
 * 
 * @see <a href="http://www.w3.org/TR/xml-names/">Namespaces in XML 1.0</a>
 *
 */
public class Namespaces {

	private Namespaces() {
	}

	/** SOAP 1.2 message envelopes. */
	public static final String SOAP_ENV = "http://www.w3.org/2003/05/soap-envelope";
	/** W3C XLink */
	public static final String XLINK = "http://www.w3.org/1999/xlink";
	/** OGC 06-121r3 (OWS 1.1) */
	public static final String OWS = "http://www.opengis.net/ows/1.1";
	/** ISO 19136 (GML 3.2) */
	public static final String GML = "http://www.opengis.net/gml/3.2";
	/** W3C XML Schema namespace */
	public static final URI XSD = URI
			.create("http://www.w3.org/2001/XMLSchema");
	/** Schematron (ISO 19757-3) namespace */
	public static final URI SCH = URI
			.create("http://purl.oclc.org/dsdl/schematron");
	/** OGC KML */
	public static final URI KML = URI.create("http://www.opengis.net/kml/2.2");
	/** Atom (RFC 4287) */
	public static final String ATOM = "http://www.w3.org/2005/Atom";

}
