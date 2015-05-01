package org.opengis.cite.kml2.validation;

import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.validation.ErrorLocator;
import org.opengis.cite.validation.ErrorSeverity;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Coordinate;

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
	 * <p>
	 * The OGC KML specifications define a compound geographic 3D coordinate
	 * reference system in Annex B.
	 * </p>
	 * 
	 * <p>
	 * The following elements may contain kml:coordinates:
	 * </p>
	 * <ul>
	 * <li>kml:Point</li>
	 * <li>kml:LineString</li>
	 * <li>kml:LinearRing</li>
	 * <li>kml:LatLonQuad</li>
	 * </ul>
	 * 
	 * @param coordsNode
	 *            An Element node (kml:coordinates).
	 * @return true if the tuple sequence is valid; false otherwise.
	 */
	public boolean isValid(Node coordsNode) {
		errHandler.reset();
		String[] tuples = coordsNode.getTextContent().trim().split("\\s+");
		Node parent = coordsNode.getParentNode();
		switch (parent.getLocalName()) {
		case "Point":
			if (tuples.length != 1) {
				errHandler.addError(
						ErrorSeverity.ERROR,
						ErrorMessage.format(ErrorMessageKeys.POINT_COORDS,
								tuples.length),
						new ErrorLocator(-1, -1, XMLUtils
								.buildXPointer(coordsNode)));
			}
			break;
		case "LineString":
			if (tuples.length < 2) {
				errHandler.addError(
						ErrorSeverity.ERROR,
						ErrorMessage.format(ErrorMessageKeys.LINE_COORDS,
								tuples.length),
						new ErrorLocator(-1, -1, XMLUtils
								.buildXPointer(coordsNode)));
			}
			break;
		case "LinearRing":
			if (tuples.length < 4) {
				errHandler.addError(
						ErrorSeverity.ERROR,
						ErrorMessage.format(ErrorMessageKeys.RING_COORDS,
								tuples.length),
						new ErrorLocator(-1, -1, XMLUtils
								.buildXPointer(coordsNode)));
			}
			if (!isClosed(tuples)) {
				errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
						.get(ErrorMessageKeys.OPEN_RING), new ErrorLocator(-1,
						-1, XMLUtils.buildXPointer(coordsNode)));
			}
			break;
		case "LatLonQuad":
			if (tuples.length != 4) {
				errHandler.addError(
						ErrorSeverity.ERROR,
						ErrorMessage.format(ErrorMessageKeys.QUAD_COORDS,
								tuples.length),
						new ErrorLocator(-1, -1, XMLUtils
								.buildXPointer(coordsNode)));
			}
			break;
		default:
			break;
		}
		for (int i = 0; i < tuples.length; i++) {
			String[] tuple = tuples[i].trim().split(",");
			if (tuple.length < 2 || tuple.length > 3) {
				errHandler.addError(
						ErrorSeverity.ERROR,
						ErrorMessage.format(ErrorMessageKeys.COORD_DIM, i,
								tuple.length),
						new ErrorLocator(-1, -1, XMLUtils
								.buildXPointer(coordsNode)));
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
									.buildXPointer(coordsNode)));
					continue;
				}
			}
		}
		return !errHandler.errorsDetected();
	}

	/**
	 * Determines if the given sequence of coordinate tuples defines a closed
	 * ring. That is, the first and last positions are coincident.
	 * 
	 * @param tuples
	 *            A sequence of coordinate tuples (2D or 3D).
	 * @return true if the first and last positions are coincident; false
	 *         otherwise.
	 */
	boolean isClosed(String[] tuples) {
		String[] firstPos = tuples[0].trim().split(",");
		double alt = (firstPos.length > 2) ? Double.parseDouble(firstPos[2])
				: 0;
		Coordinate firstCoord = new Coordinate(Double.parseDouble(firstPos[0]),
				Double.parseDouble(firstPos[1]), alt);
		String[] lastPos = tuples[tuples.length - 1].trim().split(",");
		alt = (lastPos.length > 2) ? Double.parseDouble(lastPos[2]) : 0;
		Coordinate lastCoord = new Coordinate(Double.parseDouble(lastPos[0]),
				Double.parseDouble(lastPos[1]), alt);
		return (lastCoord.equals3D(firstCoord));
	}
}
