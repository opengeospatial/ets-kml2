package org.opengis.cite.kml2.validation;

import java.util.Iterator;

import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.validation.ErrorLocator;
import org.opengis.cite.validation.ErrorSeverity;
import org.opengis.cite.validation.ValidationError;
import org.opengis.cite.validation.ValidationErrorHandler;
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
 * The applicable test cases are listed below.
 * <ul>
 * <li>ATC-138: Region visibility (LOD)</li>
 * <li>ATC-140: Region</li>
 * </ul>
 */
public class RegionValidator {

	private ValidationErrorHandler errHandler;
	private GeoExtentValidator geoExtentValidator;
	private int conformanceLevel = 1;

	/**
	 * Default constructor.
	 */
	public RegionValidator() {
		this.errHandler = new ValidationErrorHandler();
		this.geoExtentValidator = new GeoExtentValidator();
	}

	/**
	 * Constructs a RegionValidator to check the constraints that apply to the
	 * specified conformance level.
	 * 
	 * @param level
	 *            The applicable conformance level.
	 */
	public RegionValidator(int level) {
		this();
		if (level > 0 && level < 4) {
			this.conformanceLevel = level;
		}
	}

	/**
	 * Returns the error messages reported during the last call to
	 * <code>validateRegionExtent</code>.
	 * 
	 * @return A String containing error messages (may be empty).
	 */
	public String getErrorMessages() {
		return errHandler.toString();
	}

	/**
	 * Validates a kml:Region element.
	 * 
	 * @param node
	 *            A kml:Region element.
	 * @return true if the region satisfies all applicable constraints; false
	 *         otherwise.
	 */
	public boolean isValid(Node node) {
		errHandler.reset();
		Element region = (Element) node;
		checkRegionExtent(region);
		checkRegionVisibility(region);
		if (this.conformanceLevel > 2) {
			checkLodFadeRange(region);
		}
		return !errHandler.errorsDetected();
	}

	/**
	 * Checks the spatial extent of a Region (kml:LatLonAltBox). The default
	 * extent of a region spans the entire surface of the EGM96 geoid.
	 * 
	 * @param node
	 *            An Element node that contains a kml:Region element.
	 */
	void checkRegionExtent(Element region) {
		Node boxNode = region.getElementsByTagNameNS(KML2.NS_NAME,
				"LatLonAltBox").item(0);
		if (null == boxNode) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
					ErrorMessageKeys.CONSTRAINT_VIOLATION,
					"[ATC-140] Expected LatLonAtlBox"), new ErrorLocator(-1,
					-1, XMLUtils.buildXPointer(region)));
			return;
		}
		if (!geoExtentValidator.validGeoExtent(boxNode)) {
			Iterator<ValidationError> errors = geoExtentValidator.getErrors();
			while (errors.hasNext()) {
				ValidationError err = errors.next();
				errHandler.addError(
						err.getSeverity(),
						err.getMessage(),
						new ErrorLocator(-1, -1, XMLUtils
								.buildXPointer(boxNode)));
			}
		}
	}

	/**
	 * Checks that the properties of the kml:Lod element satisfy the following
	 * constraints:
	 * <ol>
	 * <li>it contains the kml:minLodPixels element</li>
	 * <li>kml:minLodPixels &lt; kml:maxLodPixels (Note: -1 denotes positive
	 * infinity)</li>
	 * </ol>
	 * 
	 * @param region
	 *            A kml:Region element
	 * 
	 * @see "OGC KML 2.3 - Abstract Test Suite, ATC-138: Region visibility (LOD)"
	 */
	void checkRegionVisibility(Element region) {
		Node lodNode = region.getElementsByTagNameNS(KML2.NS_NAME, "Lod").item(
				0);
		if (null == lodNode) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
					ErrorMessageKeys.CONSTRAINT_VIOLATION,
					"[ATC-140] Expected Lod"), new ErrorLocator(-1, -1,
					XMLUtils.buildXPointer(region)));
			return;
		}
		Node minLodPixelsNode = region.getElementsByTagNameNS(KML2.NS_NAME,
				"minLodPixels").item(0);
		if (null == minLodPixelsNode) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
					ErrorMessageKeys.CONSTRAINT_VIOLATION,
					"[ATC-138] Expected minLodPixels in Lod"),
					new ErrorLocator(-1, -1, XMLUtils.buildXPointer(region)));
			return;
		}
		double minLodPixels = Double.parseDouble(minLodPixelsNode
				.getTextContent());
		Node maxLodPixelsNode = region.getElementsByTagNameNS(KML2.NS_NAME,
				"maxLodPixels").item(0);
		double maxLodPixels = (null == maxLodPixelsNode) ? Double.POSITIVE_INFINITY
				: Double.parseDouble(maxLodPixelsNode.getTextContent());
		if (maxLodPixels == -1) {
			maxLodPixels = Double.POSITIVE_INFINITY;
		}
		if (minLodPixels >= maxLodPixels) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
					ErrorMessageKeys.CONSTRAINT_VIOLATION,
					"[ATC-138] Expected minLodPixels < maxLodPixels in Lod"),
					new ErrorLocator(-1, -1, XMLUtils.buildXPointer(region)));
		}
	}

	/**
	 * Checks that the following expression holds for the pixel ramp values that
	 * determine whether or not a region is active:
	 * 
	 * <pre>
	 * {@literal
	 * kml:minFadeExtent + kml:maxFadeExtent <= kml:maxLodPixels - kml:minLodPixels
	 * }
	 * </pre>
	 * 
	 * @param region
	 *            a kml:Region element.
	 * 
	 * @see "OGC KML 2.3 - Abstract Test Suite, ATC-306: LOD fade range"
	 */
	void checkLodFadeRange(Element region) {
		double minLodPixels = getLodParameterValue(region, "minLodPixels");
		double maxLodPixels = getLodParameterValue(region, "maxLodPixels");
		double minFadeExtent = getLodParameterValue(region, "minFadeExtent");
		double maxFadeExtent = getLodParameterValue(region, "maxFadeExtent");
		if (minFadeExtent + maxFadeExtent > maxLodPixels - minLodPixels) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
					ErrorMessageKeys.CONSTRAINT_VIOLATION,
					"[ATC-306] Region has invalid Lod fade range"),
					new ErrorLocator(-1, -1, XMLUtils.buildXPointer(region)));
		}
	}

	/**
	 * Gets the value of a Lod parameter.
	 * 
	 * @param region
	 *            A kml:Region element.
	 * @param paramName
	 *            The (local) name of some Lod parameter.
	 * @return The actual value of the parameter (or its specified default
	 *         value).
	 */
	double getLodParameterValue(Element region, String paramName) {
		double lodParamValue = (paramName.equals("maxLodPixels")) ? Double.POSITIVE_INFINITY
				: 0.0;
		Node lodParam = region.getElementsByTagNameNS(KML2.NS_NAME, paramName)
				.item(0);
		if (null != lodParam) {
			lodParamValue = Double.parseDouble(lodParam.getTextContent());
		}
		return lodParamValue;
	}
}
