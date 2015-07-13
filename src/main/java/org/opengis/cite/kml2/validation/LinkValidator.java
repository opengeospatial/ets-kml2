package org.opengis.cite.kml2.validation;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;

import javax.ws.rs.core.MediaType;

import org.opengis.cite.kml2.ETSAssert;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.HttpClientUtils;
import org.opengis.cite.kml2.util.URIUtils;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.validation.ErrorLocator;
import org.opengis.cite.validation.ErrorSeverity;
import org.opengis.cite.validation.ValidationError;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Checks that the content of a kml:Link or kml:Icon element satisfies all
 * applicable constraints. The relevant type definition is shown below.
 * 
 * <pre>
 * {@literal
 * <xsd:complexType name="LinkType" final="#all">
 *   <xsd:complexContent>
 *     <xsd:extension base="kml:BasicLinkType">
 *       <xsd:all>
 *         <xsd:element ref="kml:abstractRefreshMode" minOccurs="0"/>
 *         <xsd:element ref="kml:refreshInterval" minOccurs="0"/>
 *         <xsd:element ref="kml:abstractViewRefreshMode" minOccurs="0"/>
 *         <xsd:element ref="kml:viewRefreshTime" minOccurs="0"/>
 *         <xsd:element ref="kml:viewBoundScale" minOccurs="0"/>
 *         <xsd:element ref="kml:viewFormat" minOccurs="0"/>
 *         <xsd:element ref="kml:httpQuery" minOccurs="0"/>
 *         <xsd:element ref="kml:LinkSimpleExtensionGroup" minOccurs="0"
 *           maxOccurs="unbounded"/>
 *         <xsd:element ref="kml:LinkObjectExtensionGroup" minOccurs="0"
 *           maxOccurs="unbounded"/>
 *       </xsd:all>
 *     </xsd:extension>
 *   </xsd:complexContent>
 * </xsd:complexType>
 * }
 * </pre>
 * 
 */
public class LinkValidator {

	private int conformanceLevel = 1;
	private ValidationErrorHandler errHandler;
	private MediaType[] mediaTypes;
	private Client httpClient;

	/**
	 * Constructs a LinkValidator to check all mandatory constraints.
	 * 
	 * @param mediaTypes
	 *            A collection of acceptable media types; if null or empty any
	 *            type is acceptable.
	 */
	public LinkValidator(MediaType... mediaTypes) {
		this.errHandler = new ValidationErrorHandler();
		this.mediaTypes = mediaTypes;
		this.httpClient = HttpClientUtils.buildClient();
	}

	/**
	 * Constructs a LinkValidator to check the constraints that apply to the
	 * specified conformance level.
	 * 
	 * @param level
	 *            The applicable conformance level.
	 * @param mediaTypes
	 *            A collection of acceptable media types; if null or empty any
	 *            type is acceptable.
	 */
	public LinkValidator(int level, MediaType... mediaTypes) {
		this(mediaTypes);
		if (level > 0 && level < 4) {
			this.conformanceLevel = level;
		}
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
	 * Returns the errors reported during the last call to <code>isValid</code>.
	 * 
	 * @return An iterator over the reported validation errors.
	 */
	public Iterator<ValidationError> getErrors() {
		return errHandler.iterator();
	}

	/**
	 * Resets the error handler.
	 */
	public void reset() {
		errHandler.reset();
	}

	/**
	 * Validates a link element by checking that:
	 * <ol>
	 * <li>the URI it contains is accessible (using a HEAD request for 'http'
	 * URIs)</li>
	 * <li>the media type of the referenced resource is acceptable</li>
	 * <li>the values of various properties that affect link processing do not
	 * violate any constraints</li>
	 * </ol>
	 * 
	 * @param node
	 *            A kml:Link or kml:Icon element.
	 * @return true if the link is valid; false otherwise.
	 */
	public boolean isValid(Node node) {
		errHandler.reset();
		Element link = (Element) node;
		checkLinkReferent(link);
		checkLinkProperties(link);
		if (this.conformanceLevel > 1) {
			checkLinkConstraintsAtLevel2(link);
		}
		return !errHandler.errorsDetected();
	}

	/**
	 * Checks that the link URI (href value) refers to an accessible resource
	 * whose content type is compatible with an acceptable media type.
	 * 
	 * @param link
	 *            An Element representing a link (of type kml:LinkType).
	 */
	void checkLinkReferent(Element link) {
		NodeList hrefList = link.getElementsByTagNameNS(KML2.NS_NAME, "href");
		if (hrefList.getLength() == 0) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
					ErrorMessageKeys.MISSING_INFOSET_ITEM, "kml:href"),
					new ErrorLocator(-1, -1, XMLUtils.buildXPointer(link)));
			return;
		}
		URI uri = null;
		try {
			uri = URI.create(hrefList.item(0).getTextContent());
			if (!uri.isAbsolute()) {
				uri = URIUtils.resolveRelativeURI(link.getOwnerDocument()
						.getBaseURI(), uri.toString());
			}
			if (!uri.getScheme().equalsIgnoreCase("http")) { // file URI
				File file = new File(uri);
				if (!file.exists()) {
					throw new FileNotFoundException("File not found");
				}
			} else {
				ClientRequest req = HttpClientUtils.buildHeadRequest(uri, null,
						mediaTypes);
				ClientResponse rsp = this.httpClient.handle(req);
				if (rsp.getStatus() == HttpURLConnection.HTTP_MOVED_PERM
						|| rsp.getStatus() == HttpURLConnection.HTTP_SEE_OTHER) {
					// client won't automatically redirect from HTTP to HTTPS
					URI newURI = rsp.getLocation();
					req.setURI(newURI);
					rsp = this.httpClient.handle(req);
				}
				if (rsp.getStatus() != HttpURLConnection.HTTP_OK) {
					errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
							.format(ErrorMessageKeys.UNEXPECTED_STATUS, uri,
									rsp.getStatus()), new ErrorLocator(-1, -1,
							XMLUtils.buildXPointer(link)));
				}
				String contentType = rsp.getType().toString();
				if (!HttpClientUtils.contentIsAcceptable(contentType,
						mediaTypes)) {
					errHandler.addError(
							ErrorSeverity.ERROR,
							ErrorMessage.format(
									ErrorMessageKeys.UNACCEPTABLE_MEDIA_TYPE,
									contentType, Arrays.toString(mediaTypes)),
							new ErrorLocator(-1, -1, XMLUtils
									.buildXPointer(link)));
				}
			}
		} catch (Exception e) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
					ErrorMessageKeys.URI_NOT_ACCESSIBLE, uri, e.getMessage()),
					new ErrorLocator(-1, -1, XMLUtils.buildXPointer(link)));
		}
	}

	/**
	 * Checks various properties that affect link processing.
	 * 
	 * @param link
	 *            An Element representing a link (of type kml:LinkType).
	 * 
	 * @see "OGC 14-068, ATC-109: Link properties"
	 */
	void checkLinkProperties(Element link) {
		Node refresh = link.getElementsByTagNameNS(KML2.NS_NAME,
				"refreshInterval").item(0);
		if (null != refresh
				&& Double.parseDouble(refresh.getTextContent()) <= 0) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
					ErrorMessageKeys.CONSTRAINT_VIOLATION,
					"kml:refreshInterval > 0"), new ErrorLocator(-1, -1,
					XMLUtils.buildXPointer(link)));
		}
		Node viewRefresh = link.getElementsByTagNameNS(KML2.NS_NAME,
				"viewRefreshTime").item(0);
		if (null != viewRefresh
				&& Double.parseDouble(viewRefresh.getTextContent()) <= 0) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
					ErrorMessageKeys.CONSTRAINT_VIOLATION,
					"kml:viewRefreshTime > 0"), new ErrorLocator(-1, -1,
					XMLUtils.buildXPointer(link)));
		}
		Node viewBound = link.getElementsByTagNameNS(KML2.NS_NAME,
				"viewBoundScale").item(0);
		if (null != viewBound
				&& Double.parseDouble(viewBound.getTextContent()) <= 0) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
					ErrorMessageKeys.CONSTRAINT_VIOLATION,
					"kml:viewBoundScale > 0"), new ErrorLocator(-1, -1,
					XMLUtils.buildXPointer(link)));
		}
	}

	/**
	 * Checks that all link constraints defined for CL2 are satisfied. The
	 * applicable test cases are listed below.
	 * <ul>
	 * <li>ATC-205: viewFormat element not empty</li>
	 * <li>ATC-206: httpQuery element not empty</li>
	 * <li>ATC-210: Link refresh mode</li>
	 * </ul>
	 * 
	 * @param link
	 *            An Element representing a link (of type kml:LinkType).
	 */
	void checkLinkConstraintsAtLevel2(Element link) {
		Node viewFormat = link.getElementsByTagNameNS(KML2.NS_NAME,
				"viewFormat").item(0);
		if (null != viewFormat && viewFormat.getTextContent().isEmpty()) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
					ErrorMessageKeys.CONSTRAINT_VIOLATION,
					"kml:viewFormat is not empty"), new ErrorLocator(-1, -1,
					XMLUtils.buildXPointer(link)));
		}
		Node httpQuery = link.getElementsByTagNameNS(KML2.NS_NAME, "httpQuery")
				.item(0);
		if (null != httpQuery && httpQuery.getTextContent().isEmpty()) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
					ErrorMessageKeys.CONSTRAINT_VIOLATION,
					"kml:httpQuery is not empty"), new ErrorLocator(-1, -1,
					XMLUtils.buildXPointer(link)));
		}
		try { // from ATC-210
			ETSAssert
					.assertXPath(
							"not(kml:refreshInterval) or kml:refreshMode = 'onInterval'",
							link, null);
			ETSAssert.assertXPath(
					"not(kml:viewRefreshTime) or kml:refreshMode = 'onStop'",
					link, null);
		} catch (AssertionError e) {
			errHandler.addError(ErrorSeverity.ERROR, e.getMessage(),
					new ErrorLocator(-1, -1, XMLUtils.buildXPointer(link)));
		}
	}

}
