package org.opengis.cite.kml2.validation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

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
 * Validates the content of an element that represents a geographic extent (that
 * is, it substitutes directly or indirectly for kml:AbstractExtentGroup). The
 * allowed elements include the following, although not all are permitted in
 * every context:
 * <ul>
 * <li>kml:LatLonBox</li>
 * <li>kml:LatLonAltBox</li>
 * <li>kml:LatLonQuad</li>
 * </ul>
 * 
 */
public class GeoExtentValidator {

	private static final String BOX_NORTH = "north";
	private static final String BOX_SOUTH = "south";
	private static final String BOX_EAST = "east";
	private static final String BOX_WEST = "west";
	private static final String BOX_MIN_ALT = "minAltitude";
	private static final String BOX_MAX_ALT = "maxAltitude";
	private static final Map<String, Double> DEFAULT_BOX = initDefaultBox();
	private ValidationErrorHandler errHandler;

	private static Map<String, Double> initDefaultBox() {
		Map<String, Double> defaultBox = new HashMap<>();
		defaultBox.put(BOX_NORTH, 90.0);
		defaultBox.put(BOX_SOUTH, -90.0);
		defaultBox.put(BOX_EAST, 180.0);
		defaultBox.put(BOX_WEST, -180.0);
		defaultBox.put(BOX_MIN_ALT, 0.0);
		defaultBox.put(BOX_MAX_ALT, 0.0);
		return Collections.unmodifiableMap(defaultBox);
	}

	/**
	 * Constructor with default error handler.
	 */
	public GeoExtentValidator() {
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

	public boolean validGeoExtent(Node node) {
		errHandler.reset();
		boolean isValid = false;
		if (node.getLocalName().endsWith("Box")) {
			isValid = validateBox(node);
		}
		return isValid;
	}

	/**
	 * Validates a bounding box. The content of a kml:LatLon[Alt]Box element
	 * must satisfy all of the following constraints:
	 * <ol>
	 * <li>kml:north > kml:south;</li>
	 * <li>kml:east > kml:west;</li>
	 * <li>kml:minAltitude <= kml:maxAltitude;</li>
	 * <li>if kml:minAltitude and kml:maxAltitude are both present, then
	 * kml:altitudeMode does not have the value "clampToGround".</li>
	 * </ol>
	 * 
	 * <p>
	 * Some additional constraints were introduced in KML 2.3 with the longitude
	 * range extension (to &#177; 360):
	 * </p>
	 * <ul>
	 * <li>kml:east - kml:west <= 360 (non-self-overlap)</li>
	 * <li>if |kml:west| > 180 or |kml:east| > 180, then kml:east > 0 and
	 * kml:west < 180 (uniqueness)</li>
	 * </ul>
	 * 
	 * @param boxNode
	 *            An Element node that contains a bounding box element
	 *            (kml:LatLon[Alt]Box).
	 * @return true if the box satisfies all constraints; false otherwise.
	 */
	boolean validateBox(Node boxNode) {
		if (null == boxNode || !boxNode.getLocalName().startsWith("LatLon")) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
					ErrorMessageKeys.MISSING_INFOSET_ITEM, "kml:LatLon*"),
					new ErrorLocator(-1, -1, XMLUtils.buildXPointer(boxNode)));
			return false;
		}
		Map<String, Double> boxProps = getNumericProperties(boxNode,
				DEFAULT_BOX);
		if (boxProps.get(BOX_NORTH) <= boxProps.get(BOX_SOUTH)) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
					ErrorMessageKeys.CONSTRAINT_VIOLATION,
					"[kml:LatLonAltBox] kml:north > kml:south"),
					new ErrorLocator(-1, -1, XMLUtils.buildXPointer(boxNode)));
		}
		if (boxProps.get(BOX_EAST) <= boxProps.get(BOX_WEST)) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
					ErrorMessageKeys.CONSTRAINT_VIOLATION,
					"[kml:LatLonAltBox] kml:east > kml:west"),
					new ErrorLocator(-1, -1, XMLUtils.buildXPointer(boxNode)));
		}
		if (boxProps.get(BOX_MIN_ALT) > boxProps.get(BOX_MAX_ALT)) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
					ErrorMessageKeys.CONSTRAINT_VIOLATION,
					"[kml:LatLonAltBox] kml:minAltitude <= kml:maxAltitude"),
					new ErrorLocator(-1, -1, XMLUtils.buildXPointer(boxNode)));
		}
		if (boxProps.get(BOX_EAST) - boxProps.get(BOX_WEST) > 360) {
			errHandler
					.addError(
							ErrorSeverity.ERROR,
							ErrorMessage
									.format(ErrorMessageKeys.CONSTRAINT_VIOLATION,
											"[kml:LatLonAltBox] kml:east - kml:west <= 360 (non-self-overlap)"),
							new ErrorLocator(-1, -1, XMLUtils
									.buildXPointer(boxNode)));
		}
		if (Math.abs(boxProps.get(BOX_WEST)) > 180
				|| Math.abs(boxProps.get(BOX_EAST)) > 180) {
			if (boxProps.get(BOX_EAST) <= 0 || boxProps.get(BOX_WEST) >= 180) {
				errHandler
						.addError(
								ErrorSeverity.ERROR,
								ErrorMessage
										.format(ErrorMessageKeys.CONSTRAINT_VIOLATION,
												"[kml:LatLonAltBox] kml:east > 0 and kml:west < 180 (uniqueness)"),
								new ErrorLocator(-1, -1, XMLUtils
										.buildXPointer(boxNode)));
			}
		}
		if ((boolean) XMLUtils.evaluateXPath(boxNode,
				"kml:minAltitude and kml:maxAltitude", null,
				XPathConstants.BOOLEAN)) {
			Element latLonAltBox = Element.class.cast(boxNode);
			Node altMode = latLonAltBox.getElementsByTagNameNS(KML2.NS_NAME,
					"altitudeMode").item(0);
			String altModeValue = (null != altMode) ? altMode.getTextContent()
					.trim() : KML2.DEFAULT_ALT_MODE;
			if (altModeValue.equals(KML2.DEFAULT_ALT_MODE)) {
				errHandler
						.addError(
								ErrorSeverity.ERROR,
								ErrorMessage
										.format(ErrorMessageKeys.CONSTRAINT_VIOLATION,
												"[kml:LatLonAltBox] kml:altitudeMode != 'clampToGround'"),
								new ErrorLocator(-1, -1, XMLUtils
										.buildXPointer(boxNode)));
			}

		}
		return !errHandler.errorsDetected();
	}

	/**
	 * Builds a Map containing the numeric values conveyed by the child elements
	 * of the given node. The keys are the local names of the elements that
	 * contain a parsable number (double).
	 * 
	 * @param node
	 *            A node expected to contain child elements.
	 * @param defaults
	 *            A collection of default values.
	 * @return A Map containing numeric values.
	 */
	Map<String, Double> getNumericProperties(Node node,
			Map<String, Double> defaults) {
		if (null == defaults) {
			defaults = new HashMap<String, Double>();
		}
		Map<String, Double> properties = new HashMap<>(defaults);
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			try {
				Double value = Double.valueOf(child.getTextContent());
				properties.put(child.getLocalName(), value);
			} catch (NumberFormatException ex) {
				continue;
			}
		}
		return properties;
	}

}
