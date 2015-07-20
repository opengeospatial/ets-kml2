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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Checks that the content of a kml:Track element satisfies all applicable
 * constraints. The relevant type definition is shown below (with extension
 * points omitted).
 * 
 * <pre>
 * {@literal
 * <complexType name="TrackType">
 *   <complexContent>
 *     <extension base="kml:AbstractGeometryType">
 *       <all>
 *         <element ref="kml:extrude" minOccurs="0"/>
 *         <element ref="kml:tessellate" minOccurs="0"/>
 *         <group ref="kml:AltitudeModeGroup"/>
 *         <element ref="kml:when" minOccurs="0" maxOccurs="unbounded"/>
 *         <element ref="kml:coord" minOccurs="0" maxOccurs="unbounded"/>
 *         <element ref="kml:angles" minOccurs="0" maxOccurs="unbounded"/>
 *         <element ref="kml:Model" minOccurs="0"/>
 *         <element ref="kml:ExtendedData" minOccurs="0"/>
 *       </all>
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }
 * </pre>
 * 
 */
public class TrackValidator {

	static final Envelope CRS_EXTENT;
	static {
		CRS_EXTENT = new Envelope(-180, 180, -90, 90);
	}
	ValidationErrorHandler errHandler;

	/**
	 * Default constructor.
	 */
	public TrackValidator() {
		this.errHandler = new ValidationErrorHandler();
	}

	/**
	 * Returns the error messages reported during the last call to
	 * <code>isValid</code>.
	 * 
	 * @return A String containing error messages (may be empty).
	 */
	public String getErrorMessages() {
		return errHandler.toString();
	}

	/**
	 * Resets the error handler.
	 */
	public void reset() {
		errHandler.reset();
	}

	/**
	 * Validates the content of a kml:Track element by checking the constraints
	 * defined by the following test cases:
	 * <ol>
	 * <li>ATC-146: Track positions</li>
	 * <li>ATC-147: Track properties constitute parallel array</li>
	 * <li>ATC-148: Track orientations</li>
	 * </ol>
	 * 
	 * @param node
	 *            A kml:Track element.
	 * @return true if the track satisfies all applicable constraints; false
	 *         otherwise.
	 */
	public boolean isValid(Node node) {
		errHandler.reset();
		Element track = (Element) node;
		checkCoordList(track);
		// ATC-147: Track properties constitute parallel array
		if (track.getElementsByTagNameNS(KML2.NS_NAME, "when").getLength() != track
				.getElementsByTagNameNS(KML2.NS_NAME, "coord").getLength()) {
			errHandler
					.addError(
							ErrorSeverity.ERROR,
							ErrorMessage
									.format(ErrorMessageKeys.CONSTRAINT_VIOLATION,
											"Expected count(kml:when) = count(kml:coord) in Track",
											""), new ErrorLocator(-1, -1,
									XMLUtils.buildXPointer(track)));
		}
		checkAngles(track);
		return !errHandler.errorsDetected();
	}

	/**
	 * Verifies that the coordinates specifying the track positions (kml:coord)
	 * consist of space-delimited numeric values in the applicable coordinate
	 * reference system (default CRS).
	 * 
	 * @param track
	 *            A kml:Track element
	 * 
	 * @see "OGC 14-068r1: OGC KML 2.3 - Abstract Test Suite, ATC-146"
	 */
	void checkCoordList(Element track) {
		NodeList coordList = track
				.getElementsByTagNameNS(KML2.NS_NAME, "coord");
		if (coordList.getLength() == 0) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
					ErrorMessageKeys.CONSTRAINT_VIOLATION,
					"Expected kml:coord in Track", ""), new ErrorLocator(-1,
					-1, XMLUtils.buildXPointer(track)));
			return;
		}
		for (int i = 0; i < coordList.getLength(); i++) {
			Node coordNode = coordList.item(i);
			if (coordNode.getTextContent().isEmpty()) {
				continue; // permitted to indicate missing data
			}
			double[] coord = getNumericValues(coordNode);
			if (coord.length != 3) {
				errHandler.addError(
						ErrorSeverity.ERROR,
						ErrorMessage.format(ErrorMessageKeys.COORD_DIM, i, "3",
								coord.length),
						new ErrorLocator(-1, -1, XMLUtils
								.buildXPointer(coordNode)));
				continue;
			}
			Coordinate jtsCoord = new Coordinate(coord[0], coord[1], coord[2]);
			if (!CRS_EXTENT.intersects(jtsCoord)) {
				errHandler.addError(
						ErrorSeverity.ERROR,
						ErrorMessage.format(ErrorMessageKeys.OUTSIDE_CRS,
								jtsCoord.toString()),
						new ErrorLocator(-1, -1, XMLUtils
								.buildXPointer(coordNode)));
			}
		}
	}

	/**
	 * Verifies that each kml:angles element contained by a Track satisfies the
	 * following constraints: (a) it contains a space-delimited list of numeric
	 * values; (b) if the track has no associated model, then only one angle
	 * (heading) is specified.
	 * 
	 * @param track
	 *            A kml:Track element
	 * 
	 * @see "OGC 14-068r1: OGC KML 2.3 - Abstract Test Suite, ATC-148"
	 */
	void checkAngles(Element track) {
		NodeList anglesList = track.getElementsByTagNameNS(KML2.NS_NAME,
				"angles");
		for (int i = 0; i < anglesList.getLength(); i++) {
			Node anglesNode = anglesList.item(i);
			if (anglesNode.getTextContent().isEmpty()) {
				continue; // permitted to indicate missing data
			}
			double[] angles = getNumericValues(anglesNode);
			if (null == track.getElementsByTagNameNS(KML2.NS_NAME, "Model")
					.item(0) && (angles.length > 1)) {
				errHandler
						.addError(
								ErrorSeverity.ERROR,
								ErrorMessage
										.format(ErrorMessageKeys.CONSTRAINT_VIOLATION,
												"[ATC-148] Expected one angle value, since Track has no Model",
												""), new ErrorLocator(-1, -1,
										XMLUtils.buildXPointer(anglesNode)));
				continue;
			}
		}
	}

	/**
	 * Gets the content of the given node as an array of numeric values.
	 * 
	 * @param node
	 *            A node containing a sequence of space-separated values, all of
	 *            which are presumed to be a parsable double.
	 * @return A double[] array.
	 */
	double[] getNumericValues(Node node) {
		String[] values = node.getTextContent().trim().split("\\s+");
		double[] numericValues = new double[values.length];
		for (int j = 0; j < values.length; j++) {
			try {
				numericValues[j] = Double.parseDouble(values[j]);
			} catch (NumberFormatException e) {
				errHandler.addError(ErrorSeverity.ERROR,
						ErrorMessage.format(ErrorMessageKeys.NAN, values[j]),
						new ErrorLocator(-1, -1, XMLUtils.buildXPointer(node)));
				continue;
			}
		}
		return numericValues;
	}

}
