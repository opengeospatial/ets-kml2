package org.opengis.cite.kml2.validation;

import java.net.URI;
import java.net.URL;

import javax.ws.rs.core.MediaType;
import javax.xml.transform.dom.DOMSource;

import org.opengis.cite.kml2.ETSAssert;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.validation.ErrorLocator;
import org.opengis.cite.validation.ErrorSeverity;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The relevant type definition for kml:Update is shown below; it may appear
 * within a NetworkLinkControl or AnimatedUpdate element. The elements that may
 * substitute for <em>kml:AbstractUpdateOptionGroup</em> are kml:Create,
 * kml:Change and kml:Delete.
 * 
 * <pre>
 * {@literal
 * <xsd:complexType name="UpdateType" final="#all">
 *   <xsd:all>
 *     <xsd:element ref="kml:targetHref"/>
 *     <xsd:element ref="kml:AbstractUpdateOptionGroup" minOccurs="1" maxOccurs="unbounded"/>
 *     <xsd:element ref="kml:UpdateExtensionGroup" minOccurs="0" maxOccurs="unbounded"/>
 *   </xsd:all>
 *   <xsd:anyAttribute namespace="##other" processContents="lax"/>
 * </xsd:complexType>
 * }
 * </pre>
 * 
 * The applicable test cases are listed below.
 * <ul>
 * <li>ATC-122: Update referent</li>
 * <li>ATC-123: Update targets</li>
 * <li>ATC-145: AnimatedUpdate referent</li>
 * </ul>
 */
public class UpdateValidator {

	ValidationErrorHandler errHandler;

	/**
	 * Default constructor.
	 */
	public UpdateValidator() {
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
	 * Checks that a kml:Update element satisfies all applicable constraints. In
	 * particular:
	 * <ol>
	 * <li>if it is not empty, the kml:targetHref element contains an absolute
	 * URI that refers to a KML or KMZ resource.</li>
	 * <li>All KML objects that are the target of an update action (kml:Create,
	 * kml:Delete, kml:Change) have a {@code targetId} attribute and do not have
	 * an {@code id} attribute</li>
	 * </ol>
	 * 
	 * In the context of an AnimatedUpdate appearing in a Tour, an empty
	 * Update/targetHref element indicates that all elements to be updated occur
	 * in the same document.
	 * 
	 * @param updateNode
	 *            A node that represents a kml:Update element.
	 * @return true if no constraints are violated; false otherwise.
	 */
	public boolean isValid(Node updateNode) {
		errHandler.reset();
		Element update = (Element) updateNode;
		Element targetUri = (Element) update.getElementsByTagNameNS(
				KML2.NS_NAME, "targetHref").item(0);
		try {
			if (!targetUri.getTextContent().trim().isEmpty()) {
				ETSAssert.assertReferentExists(
						URI.create(targetUri.getTextContent().trim()),
						MediaType.valueOf(KML2.KML_MEDIA_TYPE),
						MediaType.valueOf(KML2.KMZ_MEDIA_TYPE));
			}
			URL schRef = this.getClass().getResource(
					"/org/opengis/cite/kml2/sch/kml-update.sch");
			ETSAssert.assertSchematronValid(schRef, new DOMSource(update));
		} catch (AssertionError e) {
			errHandler.addError(ErrorSeverity.ERROR, e.getMessage(),
					new ErrorLocator(-1, -1, XMLUtils.buildXPointer(update)));
		}
		return !errHandler.errorsDetected();
	}
}
