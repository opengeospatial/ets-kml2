package org.opengis.cite.kml2.validation;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.kml2.ETSAssert;
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
import org.w3c.dom.NodeList;

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
	 * Immutable set of all NetworkLink states (ListStyle/ItemIcon).
	 */
	static final Set<String> NETWORK_LINK_STATE_SET;
	static {
		Set<String> aSet = new HashSet<String>();
		aSet.add("error");
		aSet.add("fetching0");
		aSet.add("fetching1");
		aSet.add("fetching2");
		NETWORK_LINK_STATE_SET = Collections.unmodifiableSet(aSet);
	}

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
		checkListStyle(style);
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

	/**
	 * Checks that a kml:ListStyle element satisfies all applicable constraints.
	 * 
	 * @param style
	 *            A kml:Style element.
	 * 
	 * @see "ATC-136: ItemIcon refers to image resource"
	 */
	void checkListStyle(Element style) {
		NodeList itemIcons = null;
		try {
			itemIcons = XMLUtils.evaluateXPath(style,
					"kml:ListStyle/kml:ItemIcon", null);
		} catch (XPathExpressionException e) {
		}
		for (int i = 0; i < itemIcons.getLength(); i++) {
			Element itemIcon = (Element) itemIcons.item(i);
			Node href = itemIcon.getElementsByTagNameNS(KML2.NS_NAME, "href")
					.item(0);
			URI uri = URI.create(href.getTextContent().trim());
			if (!uri.isAbsolute()) {
				uri = uri.resolve(style.getOwnerDocument().getBaseURI());
			}
			try {
				ETSAssert.assertReferentExists(uri,
						MediaType.valueOf("image/*"));
			} catch (AssertionError e) {
				errHandler.addError(
						ErrorSeverity.ERROR,
						e.getMessage(),
						new ErrorLocator(-1, -1, XMLUtils
								.buildXPointer(itemIcon)));
			}
			Node state = itemIcon.getElementsByTagNameNS(KML2.NS_NAME, "state")
					.item(0);
			if (null == state) {
				continue;
			}
			List<String> stateList = Arrays.asList(state.getTextContent()
					.trim().split("\\s"));
			Set<String> states = new HashSet<>(stateList);
			// set intersection to determine presence of NetworkLink state
			states.retainAll(NETWORK_LINK_STATE_SET);
			if (!states.isEmpty() && style.getAttribute("id").isEmpty()) {
				// not in a shared style definition so check parent
				QName feature = XMLUtils.getQName(style.getParentNode());
				if (!feature.equals(new QName(KML2.NS_NAME, "NetworkLink"))) {
					errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
							.format(ErrorMessageKeys.CONSTRAINT_VIOLATION,
									"[ATC-136] ListStyle applies to NetworkLink, not "
											+ feature), new ErrorLocator(-1,
							-1, XMLUtils.buildXPointer(itemIcon)));
				}
			}
		}
	}
}