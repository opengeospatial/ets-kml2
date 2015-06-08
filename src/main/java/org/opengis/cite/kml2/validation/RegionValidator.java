package org.opengis.cite.kml2.validation;

import org.opengis.cite.kml2.KML2;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The relevant type definition is shown below (with extension points omitted);
 * the spatial extent of the region must be specified using a kml:LatLonAltBox
 * element.
 * 
 * <pre>
 * {@literal
 * <xsd:complexType name="RegionType" final="#all">
 *   <xsd:complexContent>
 *     <xsd:extension base="kml:AbstractObjectType">
 *       <xsd:all>
 *         <element ref="kml:AbstractExtentGroup" minOccurs="0"/>
 *         <element ref="kml:Lod" minOccurs="0"/>
 *       </xsd:all>
 *     </xsd:extension>
 *   </xsd:complexContent>
 * </xsd:complexType>
 * }
 * </pre>
 * 
 */
public class RegionValidator {

	private GeoExtentValidator geoExtentValidator;

	/**
	 * Default constructor.
	 */
	public RegionValidator() {
		this.geoExtentValidator = new GeoExtentValidator();
	}

	/**
	 * Returns the error messages reported during the last call to
	 * <code>validateRegionExtent</code>.
	 * 
	 * @return A String containing error messages (may be empty).
	 */
	public String getErrors() {
		return geoExtentValidator.toString();
	}

	/**
	 * Validates the spatial extent of a Region (kml:LatLonAltBox). The default
	 * extent of a region spans the entire surface of the EGM96 geoid.
	 * 
	 * @param node
	 *            An Element node that contains a kml:Region element.
	 * @return true if the region has a valid extent (kml:LatLonAltBox); false
	 *         otherwise.
	 */
	public boolean validateRegionExtent(Node node) {
		Element region = (Element) node;
		Node boxNode = region.getElementsByTagNameNS(KML2.NS_NAME,
				"LatLonAltBox").item(0);
		return geoExtentValidator.validGeoExtent(boxNode);
	}
}
