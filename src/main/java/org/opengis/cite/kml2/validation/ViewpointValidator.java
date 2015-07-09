package org.opengis.cite.kml2.validation;

import java.net.URL;

import javax.xml.transform.dom.DOMSource;

import org.opengis.cite.kml2.ETSAssert;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.validation.ErrorLocator;
import org.opengis.cite.validation.ErrorSeverity;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The relevant type definition for kml:CameraType is shown below, with
 * extension points omitted (kml:LookAtType is very similar, with kml:range in
 * place of kml:roll). The kml:Camera and kml:LookAt elements define viewpoints
 * on associated features.
 * 
 * <pre>
 * {@literal
 * <complexType name="CameraType" final="#all">
 *   <complexContent>
 *     <extension base="kml:AbstractViewType">
 *       <all>
 *         <element ref="kml:longitude" minOccurs="0"/>
 *         <element ref="kml:latitude" minOccurs="0"/>
 *         <element ref="kml:altitude" minOccurs="0"/>
 *         <element ref="kml:heading" minOccurs="0"/>
 *         <element ref="kml:tilt" minOccurs="0"/>
 *         <element ref="kml:roll" minOccurs="0"/>
 *         <group ref="kml:AltitudeModeGroup"/>
 *         <element ref="kml:horizFov" minOccurs="0"/>
 *       </all>
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }
 * </pre>
 * 
 * The relevant test cases are listed below.
 * <ul>
 * <li>ATC-213: Camera position</li>
 * </ul>
 */
public class ViewpointValidator {

	ValidationErrorHandler errHandler;

	/**
	 * Default constructor.
	 */
	public ViewpointValidator() {
		this.errHandler = new ValidationErrorHandler();
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
	 * Checks that a kml:Camera or kml:LookAt element satisfies all applicable
	 * constraints. These elements (kml:AbstractViewGroup) may appear in the
	 * context of a KML feature, NetworkLinkControl, or FlyTo element.
	 * 
	 * @param viewNode
	 *            A node that represents a kml:Camera or kml:LookAt element.
	 * @return true if no constraints are violated; false otherwise.
	 */
	public boolean isValid(Node viewNode) {
		errHandler.reset();
		Element view = (Element) viewNode;
		try {
			URL schRef = this.getClass().getResource(
					"/org/opengis/cite/kml2/sch/kml-viewpoint.sch");
			ETSAssert.assertSchematronValid(schRef, new DOMSource(view));
		} catch (AssertionError e) {
			errHandler.addError(ErrorSeverity.ERROR, e.getMessage(),
					new ErrorLocator(-1, -1, XMLUtils.buildXPointer(view)));
		}
		return !errHandler.errorsDetected();
	}
}
