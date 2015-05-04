package org.opengis.cite.kml2.validation;

import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.validation.ErrorLocator;
import org.opengis.cite.validation.ErrorSeverity;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Checks that the content of a kml:coordinates element satisfies all applicable
 * constraints. The following elements may contain coordinates:
 * <ul>
 * <li>kml:Point</li>
 * <li>kml:LineString</li>
 * <li>kml:LinearRing</li>
 * <li>kml:LatLonQuad</li>
 * </ul>
 *
 * The content of a kml:coordinates element is a list of white space-separated
 * 2D or 3D tuples that contain comma-separated decimal values (lon,lat[,hgt]).
 * The relevant schema components are shown below.The relevant schema components
 * are shown below.
 * 
 * <pre>
 * {@literal
 * <xsd:element name="coordinates" type="kml:coordinatesType"/>
 * <xsd:simpleType name="coordinatesType">
 *   <xsd:list itemType="string"/>
 * </xsd:simpleType>
 * }
 * </pre>
 * 
 * <p>
 * The OGC KML specifications define a compound geographic 3D coordinate
 * reference system in Annex B.
 * </p>
 * 
 */
public class CoordinatesValidator {

	ValidationErrorHandler errHandler;

	public CoordinatesValidator() {
		this.errHandler = new ValidationErrorHandler();
	}

	/**
	 * Returns the error messages reported during the last call to
	 * <code>isValid</code>.
	 * 
	 * @return A String containing error messages (may be empty).
	 */
	public String getErrors() {
		return errHandler.toString();
	}

	/**
	 * Resets the error handler.
	 */
	public void reset() {
		errHandler.reset();
	}

	/**
	 * Validates coordinate tuples by checking the following:
	 * <ol>
	 * <li>the length of the tuple sequence</li>
	 * <li>coordinate values are numeric</li>
	 * <li>tuple dimension (must be 2-3)</li>
	 * </ol>
	 * 
	 * @param node
	 *            An Element node that contains a kml:coordinates element.
	 * @return true if the tuple sequence is valid; false otherwise.
	 */
	public boolean isValid(Node node) {
		errHandler.reset();
		Element elem = (Element) node;
		NodeList coords = elem.getElementsByTagNameNS(KML2.NS_NAME,
				"coordinates");
		if (coords.getLength() == 0) {
			errHandler.addError(ErrorSeverity.ERROR,
					ErrorMessage.get(ErrorMessageKeys.MISSING_COORDS),
					new ErrorLocator(-1, -1, XMLUtils.buildXPointer(node)));
			return false;
		}
		String[] tuples = coords.item(0).getTextContent().trim().split("\\s+");
		switch (node.getLocalName()) {
		case "Point":
			if (tuples.length != 1) {
				errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
						ErrorMessageKeys.POINT_COORDS, tuples.length),
						new ErrorLocator(-1, -1, XMLUtils.buildXPointer(node)));
			}
			break;
		case "LineString":
			if (tuples.length < 2) {
				errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
						ErrorMessageKeys.LINE_COORDS, tuples.length),
						new ErrorLocator(-1, -1, XMLUtils.buildXPointer(node)));
			}
			break;
		case "LinearRing":
			if (tuples.length < 4) {
				errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
						ErrorMessageKeys.RING_COORDS, tuples.length),
						new ErrorLocator(-1, -1, XMLUtils.buildXPointer(node)));
			}
			break;
		case "LatLonQuad":
			if (tuples.length != 4) {
				errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
						ErrorMessageKeys.QUAD_COORDS, tuples.length),
						new ErrorLocator(-1, -1, XMLUtils.buildXPointer(node)));
			}
			break;
		default:
			break;
		}
		for (int i = 0; i < tuples.length; i++) {
			String[] tuple = tuples[i].trim().split(",");
			if (tuple.length < 2 || tuple.length > 3) {
				errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
						ErrorMessageKeys.COORD_DIM, i, tuple.length),
						new ErrorLocator(-1, -1, XMLUtils.buildXPointer(node)));
				continue;
			}
			for (String val : tuple) {
				try {
					Float.parseFloat(val);
				} catch (NumberFormatException e) {
					errHandler.addError(
							ErrorSeverity.ERROR,
							ErrorMessage.format(ErrorMessageKeys.NAN, val),
							new ErrorLocator(-1, -1, XMLUtils
									.buildXPointer(node)));
					continue;
				}
			}
		}
		return !errHandler.errorsDetected();
	}

}
