package org.opengis.cite.kml2.validation;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Iterator;

import javax.ws.rs.core.MediaType;

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

	ValidationErrorHandler errHandler;
	MediaType[] mediaTypes;

	/**
	 * Constructs a LinkValidator to check the specified constraints.
	 * 
	 * @param mediaTypes
	 *            A collection of acceptable media types; if null or empty any
	 *            type is acceptable.
	 */
	public LinkValidator(MediaType... mediaTypes) {
		this.errHandler = new ValidationErrorHandler();
		this.mediaTypes = mediaTypes;
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
	 *            An Element node that contains a kml:Link element.
	 * @return true if the link is valid; false otherwise.
	 */
	public boolean isValid(Node node) {
		errHandler.reset();
		Element link = (Element) node;
		checkLinkReferent(link);
		checkLinkProperties(link);
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
			URLConnection urlConn = uri.toURL().openConnection();
			if (!uri.getScheme().equalsIgnoreCase("http")) {
				try (InputStream inStream = urlConn.getInputStream()) {
					// don't try to read file content
				}
			} else {
				HttpURLConnection httpConn = (HttpURLConnection) urlConn;
				httpConn.setRequestMethod("HEAD");
				httpConn.setConnectTimeout(5000);
				StringBuilder acceptHeaderVal = new StringBuilder();
				for (MediaType type : mediaTypes) {
					acceptHeaderVal.append(type).append(',');
				}
				httpConn.setRequestProperty("Accept",
						acceptHeaderVal.toString());
				if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					errHandler.addError(
							ErrorSeverity.ERROR,
							ErrorMessage.format(
									ErrorMessageKeys.UNEXPECTED_STATUS, uri),
							new ErrorLocator(-1, -1, XMLUtils
									.buildXPointer(link)));
				}
				String contentType = urlConn.getContentType();
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

}
