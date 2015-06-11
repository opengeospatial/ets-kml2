package org.opengis.cite.kml2.validation;

import java.util.Iterator;

import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.validation.ErrorLocator;
import org.opengis.cite.validation.ValidationError;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Checks constraints to apply to kml:Style elements. The relevant type
 * definition is shown below (with extension points omitted).
 * 
 * <pre>
 * {@literal
 * <xsd:complexType name="StyleType" final="#all">
 *   <xsd:complexContent>
 * 	   <xsd:extension base="kml:AbstractStyleSelectorType">
 * 	     <xsd:all>
 *         <xsd:element ref="kml:IconStyle" minOccurs="0"/>
 *         <xsd:element ref="kml:LabelStyle" minOccurs="0"/>
 *         <xsd:element ref="kml:LineStyle" minOccurs="0"/>
 *         <xsd:element ref="kml:PolyStyle" minOccurs="0"/>
 *         <xsd:element ref="kml:BalloonStyle" minOccurs="0"/>
 *         <xsd:element ref="kml:ListStyle" minOccurs="0"/>
 *       </xsd:all>
 *     </xsd:extension>
 *   </xsd:complexContent>
 * </xsd:complexType>
 * }
 * </pre>
 */
public class StyleChecker {

	ValidationErrorHandler errHandler;
	private LinkValidator linkChecker;

	/**
	 * Default constructor.
	 */
	public StyleChecker() {
		this.errHandler = new ValidationErrorHandler();
		this.linkChecker = new LinkValidator(MediaType.valueOf("image/*"));
	}

	/**
	 * Returns all error messages reported during the last call to
	 * <code>isValid</code>.
	 * 
	 * @return A String containing the reported error messages (may be empty).
	 */
	public String getErrorMessages() {
		return errHandler.toString();
	}

	/**
	 * Validates a kml:Style element.
	 * 
	 * @param node
	 *            A kml:Style element.
	 * @return true if the style is valid; false otherwise.
	 */
	public boolean isValid(Node node) {
		if (!node.getLocalName().equals("Style")) {
			throw new IllegalArgumentException("Not a Style element: "
					+ node.getLocalName());
		}
		errHandler.reset();
		Element style = (Element) node;
		checkIconStyle(style);
		return !errHandler.errorsDetected();
	}

	/**
	 * Checks that a kml:IconStyle element satisfies all applicable constraints.
	 * 
	 * @param style
	 *            A kml:Style element.
	 * 
	 * @see "ATC-118: Icon element refers to image"
	 */
	void checkIconStyle(Element style) {
		Node icon = null;
		try {
			icon = XMLUtils
					.evaluateXPath(style, "kml:IconStyle/kml:Icon", null).item(
							0);
		} catch (XPathExpressionException e) {
		}
		if (null != icon && !linkChecker.isValid(icon)) {
			Iterator<ValidationError> errors = linkChecker.getErrors();
			while (errors.hasNext()) {
				ValidationError err = errors.next();
				errHandler.addError(err.getSeverity(), err.getMessage(),
						new ErrorLocator(-1, -1, XMLUtils.buildXPointer(icon)));
			}
		}
	}
}