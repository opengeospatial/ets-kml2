package org.opengis.cite.kml2;

/**
 * Contains various constants pertaining to KML 2.x documents.
 * 
 * <h3 style="margin-bottom: 0.5em">Sources</h3>
 * <ul>
 * <li><a href=
 * "https://portal.opengeospatial.org/files/?artifact_id=62042&amp;version=1"
 * target="_blank">OGC KML 2.3</a> (OGC 12-007r1)</li>
 * <li><a href="http://portal.opengeospatial.org/files/?artifact_id=27810"
 * target="_blank">OGC KML 2.2</a> (OGC 07-147r2)</li>
 * <li>Google Developers - <a
 * href="https://developers.google.com/kml/documentation/kmlreference"
 * target="_blank">KML 2.2 Reference</a></li>
 * </ul>
 */
public class KML2 {

	private KML2() {
	}

	/** The namespace name for KML 2.x. */
	public static final String NS_NAME = "http://www.opengis.net/kml/2.2";

	/** Local name of the root element in a KML document. */
	public static final String DOC_ELEMENT = "kml";

	/** Local name of the version attribute. */
	public static final String VER_ATTR = "version";

	/** KML 2.2. */
	public static final String KML_22 = "2.2";

	/** KML 2.3. */
	public static final String KML_23 = "2.3";

	/** KML coordinate reference system (see OGC 12-007r1, Annex B). */
	public static final String KML_CRS = "http://www.opengis.net/def/crs/OGC/0/LonLat84_5773";

	/**
	 * KML media type (see
	 * http://www.iana.org/assignments/media-types/application).
	 */
	public static final String KML_MEDIA_TYPE = "application/vnd.google-earth.kml+xml";

	/** KMZ media type. */
	public static final String KMZ_MEDIA_TYPE = "application/vnd.google-earth.kmz";

	/** Local name of kml:Update element. */
	public static final String KML_UPDATE = "Update";

	/** Default value of kml:altitudeMode */
	public static final String DEFAULT_ALT_MODE = "clampToGround";
}
