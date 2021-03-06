package org.opengis.cite.kml2;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.value.BooleanValue;

import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIException;
import org.apache.jena.iri.IRIFactory;
import org.opengis.cite.kml2.util.HttpClientUtils;
import org.opengis.cite.kml2.util.KMLUtils;
import org.opengis.cite.kml2.util.NamespaceBindings;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.validation.SchematronValidator;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides a set of custom assertion methods.
 */
public class ETSAssert {

	private static final Logger LOGR = Logger.getLogger(ETSAssert.class
			.getPackage().getName());

	private ETSAssert() {
	}

	/**
	 * Asserts that the qualified name of a DOM node matches one of the expected
	 * values.
	 * 
	 * @param node
	 *            The Node to check.
	 * @param expectedNames
	 *            A collection of QName objects.
	 */
	public static void assertQualifiedName(Node node, QName... expectedNames) {
		QName nodeName = XMLUtils.getQName(node);
		boolean match = false;
		for (QName qName : expectedNames) {
			if (nodeName.equals(qName)) {
				match = true;
				break;
			}
		}
		Assert.assertTrue(match, ErrorMessage.format(
				ErrorMessageKeys.UNEXPECTED_QNAME, nodeName,
				Arrays.toString(expectedNames)));
	}

	/**
	 * Asserts that an XPath 1.0 expression holds true for the given evaluation
	 * context. The following standard namespace bindings do not need to be
	 * explicitly declared:
	 * 
	 * <ul>
	 * <li>ows: {@value org.opengis.cite.kml2.Namespaces#OWS}</li>
	 * <li>xlink: {@value org.opengis.cite.kml2.Namespaces#XLINK}</li>
	 * <li>gml: {@value org.opengis.cite.kml2.Namespaces#GML}</li>
	 * </ul>
	 * 
	 * @param expr
	 *            A valid XPath 1.0 expression.
	 * @param context
	 *            The context node.
	 * @param namespaceBindings
	 *            A collection of namespace bindings for the XPath expression,
	 *            where each entry maps a namespace URI (key) to a prefix
	 *            (value). It may be {@code null}.
	 */
	public static void assertXPath(String expr, Node context,
			Map<String, String> namespaceBindings) {
		if (null == context) {
			throw new NullPointerException("Context node is null.");
		}
		NamespaceBindings bindings = NamespaceBindings.withStandardBindings();
		bindings.addAllBindings(namespaceBindings);
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(bindings);
		Boolean result;
		try {
			result = (Boolean) xpath.evaluate(expr, context,
					XPathConstants.BOOLEAN);
		} catch (XPathExpressionException xpe) {
			String msg = ErrorMessage
					.format(ErrorMessageKeys.XPATH_ERROR, expr);
			LOGR.log(Level.WARNING, msg, xpe);
			throw new AssertionError(msg);
		}
		Element elemNode;
		if (Document.class.isInstance(context)) {
			elemNode = Document.class.cast(context).getDocumentElement();
		} else {
			elemNode = (Element) context;
		}
		Assert.assertTrue(
				result,
				ErrorMessage.format(ErrorMessageKeys.XPATH_RESULT,
						elemNode.getNodeName(), expr));
	}

	/**
	 * Asserts that an XML resource is schema-valid.
	 * 
	 * @param validator
	 *            The Validator to use.
	 * @param source
	 *            The XML Source to be validated.
	 */
	public static void assertSchemaValid(Validator validator, Source source) {
		ValidationErrorHandler errHandler = new ValidationErrorHandler();
		validator.setErrorHandler(errHandler);
		try {
			validator.validate(source);
		} catch (Exception e) {
			throw new AssertionError(ErrorMessage.format(
					ErrorMessageKeys.XML_ERROR, e.getMessage()));
		}
		Assert.assertFalse(errHandler.errorsDetected(), ErrorMessage.format(
				ErrorMessageKeys.NOT_SCHEMA_VALID, errHandler.getErrorCount(),
				errHandler.toString()));
	}

	/**
	 * Asserts that an XML resource satisfies all applicable constraints
	 * specified in a Schematron (ISO 19757-3) schema. All patterns are checked.
	 * 
	 * @param schemaRef
	 *            A URL that denotes the location of a Schematron schema.
	 * @param xmlSource
	 *            The XML Source to be validated.
	 */
	public static void assertSchematronValid(URL schemaRef, Source xmlSource) {
		assertSchematronValid(schemaRef, xmlSource, "#ALL");
	}

	/**
	 * Asserts that an XML resource satisfies all applicable constraints defined
	 * for the specified phase in a Schematron (ISO 19757-3) schema. The "xslt2"
	 * query language binding is supported. Two phase names have special
	 * meanings:
	 * <ul>
	 * <li>"#ALL": All patterns are active</li>
	 * <li>"#DEFAULT": The phase identified by the defaultPhase attribute on the
	 * schema element should be used.</li>
	 * </ul>
	 * 
	 * @param schemaRef
	 *            A URL that denotes the location of a Schematron schema.
	 * @param xmlSource
	 *            The XML Source to be validated.
	 * @param activePhase
	 *            The active phase (pattern set) whose patterns are used for
	 *            validation; this is set to "#ALL" if not specified.
	 */
	public static void assertSchematronValid(URL schemaRef, Source xmlSource,
			String activePhase) {
		String phase = (null == activePhase || activePhase.isEmpty()) ? "#ALL"
				: activePhase;
		SchematronValidator validator;
		try {
			validator = new SchematronValidator(new StreamSource(
					schemaRef.toString()), phase);
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder(
					"Failed to process Schematron schema at ");
			msg.append(schemaRef).append('\n');
			msg.append(e.getMessage());
			throw new AssertionError(msg);
		}
		DOMResult result = validator.validate(xmlSource);
		Assert.assertFalse(validator.ruleViolationsDetected(), ErrorMessage
				.format(ErrorMessageKeys.NOT_SCHEMA_VALID,
						validator.getRuleViolationCount(),
						XMLUtils.writeNodeToString(result.getNode())));
	}

	/**
	 * Asserts that the given DOM node contains the expected number of
	 * descendant elements having the specified name.
	 * 
	 * @param node
	 *            A Document or Element node.
	 * @param elementName
	 *            The qualified name of the element.
	 * @param expectedCount
	 *            The expected number of occurrences.
	 */
	public static void assertDescendantElementCount(Node node,
			QName elementName, int expectedCount) {
		Element contextElem;
		switch (node.getNodeType()) {
		case Node.DOCUMENT_NODE:
			contextElem = Document.class.cast(node).getDocumentElement();
			break;
		case Node.ELEMENT_NODE:
			contextElem = Element.class.cast(node);
			break;
		default:
			return;
		}
		NodeList descendants = contextElem.getElementsByTagNameNS(
				elementName.getNamespaceURI(), elementName.getLocalPart());
		Assert.assertEquals(descendants.getLength(), expectedCount, String
				.format("Unexpected number of %s descendant elements.",
						elementName));
	}

	/**
	 * Asserts that the value of the child element kml:altitudeMode is correct
	 * according to the values of the kml:tessellate and kml:extrude elements.
	 * 
	 * @param kmlElement
	 *            A KML (geometry) element.
	 * 
	 * @see "ATC-112: Geometry extrusion"
	 * @see "ATC-113: Geometry tessellation"
	 */
	public static void assertValidAltitudeMode(Element kmlElement) {
		boolean tessellate = false;
		Node tessellateNode = kmlElement.getElementsByTagNameNS(KML2.NS_NAME,
				"tessellate").item(0);
		if (null != tessellateNode) {
			BooleanValue bool = (BooleanValue) BooleanValue
					.fromString(tessellateNode.getTextContent());
			tessellate = bool.getBooleanValue();
		}
		boolean extrude = false;
		Node extrudeNode = kmlElement.getElementsByTagNameNS(KML2.NS_NAME,
				"extrude").item(0);
		if (null != extrudeNode) {
			BooleanValue bool = (BooleanValue) BooleanValue
					.fromString(extrudeNode.getTextContent());
			extrude = bool.getBooleanValue();
		}
		Assert.assertTrue(!tessellate || !extrude, ErrorMessage.format(
				ErrorMessageKeys.CONSTRAINT_VIOLATION,
				"kml:tessellate and kml:extrude cannot both be true.",
				XMLUtils.buildXPointer(kmlElement)));
		AltitudeMode altMode = KMLUtils.getAltitudeMode(kmlElement);
		Assert.assertTrue(
				!tessellate || altMode.equals(AltitudeMode.CLAMP_TO_GROUND),
				ErrorMessage
						.format(ErrorMessageKeys.CONSTRAINT_VIOLATION,
								"kml:altitudeMode = 'clampToGround' when kml:tessellate is true.",
								XMLUtils.buildXPointer(kmlElement)));
		Assert.assertTrue(
				!extrude || !altMode.equals(AltitudeMode.CLAMP_TO_GROUND),
				ErrorMessage
						.format(ErrorMessageKeys.CONSTRAINT_VIOLATION,
								"kml:altitudeMode is not 'clampToGround' when kml:extrude is true.",
								XMLUtils.buildXPointer(kmlElement)));
	}

	/**
	 * Asserts that the resource referenced by the given URI exists and is
	 * compatible with one of the acceptable media types.
	 * 
	 * @param uri
	 *            An absolute URI based on the 'file' or 'http(s)' schemes.
	 * @param acceptableTypes
	 *            A list of acceptable media types; if empty the media range is
	 *            unconstrained (any type).
	 */
	public static void assertReferentExists(URI uri,
			MediaType... acceptableTypes) {
		if (!uri.isAbsolute()) {
			throw new AssertionError(ErrorMessage.format(
					ErrorMessageKeys.URI_NOT_ACCESSIBLE, uri,
					"The URI is not absolute"));
		}
		if (null == acceptableTypes || acceptableTypes.length == 0) {
			acceptableTypes = new MediaType[] { MediaType.WILDCARD_TYPE };
		}
		try {
			URLConnection urlConn = uri.toURL().openConnection();
			switch (uri.getScheme().toLowerCase()) {
			case "file":
				try (InputStream inStream = urlConn.getInputStream()) {
					// don't try to read file content
				}
				break;
			case "https":
			case "http":
				HttpURLConnection httpConn = (HttpURLConnection) urlConn;
				httpConn.setRequestMethod("HEAD");
				httpConn.setConnectTimeout(5000);
				StringBuilder acceptHeaderVal = new StringBuilder();
				for (MediaType type : acceptableTypes) {
					acceptHeaderVal.append(type).append(',');
				}
				httpConn.setRequestProperty("Accept",
						acceptHeaderVal.toString());
				if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					throw new AssertionError(ErrorMessage.format(
							ErrorMessageKeys.UNEXPECTED_STATUS, uri));
				}
				String contentType = urlConn.getContentType();
				if (!HttpClientUtils.contentIsAcceptable(contentType,
						acceptableTypes)) {
					throw new AssertionError(ErrorMessage.format(
							ErrorMessageKeys.UNACCEPTABLE_MEDIA_TYPE,
							contentType, Arrays.toString(acceptableTypes)));
				}
				break;
			default:
				throw new AssertionError(ErrorMessage.format(
						ErrorMessageKeys.URI_NOT_ACCESSIBLE, uri,
						"Unsupported URI scheme."));
			}
		} catch (IOException e) {
			throw new AssertionError(ErrorMessage.format(
					ErrorMessageKeys.URI_NOT_ACCESSIBLE, uri, e.getMessage()));
		}
	}

	/**
	 * Asserts that a given email address conforms to the "addr-spec" production
	 * in RFC 5322.
	 * 
	 * @param emailAddr
	 *            A String denoting an email address.
	 * 
	 * @see <a href="http://tools.ietf.org/html/rfc5322#section-3.4.1"
	 *      target="_blank">RFC 5322: Internet Message Format - Addr-Spec
	 *      Specification</a>
	 */
	public static void assertValidEmailAddress(String emailAddr) {
		Pattern emailPattern = Pattern.compile("(.+)@(.+)\\.(.+)");
		Matcher matcher = emailPattern.matcher(emailAddr);
		if (!matcher.matches()) {
			throw new AssertionError(ErrorMessage.format(
					ErrorMessageKeys.INVALID_EMAIL_ADDR, emailAddr));
		}
	}

	/**
	 * Asserts that a resource identifier is a valid internationalized resource
	 * identifier (IRI). An IRI is a sequence of characters from the Universal
	 * Character Set (Unicode/ISO 10646).
	 * 
	 * @param id
	 *            A String representing a resource identifier.
	 * 
	 * @see <a href="http://tools.ietf.org/html/rfc3987" target="_blank">RFC
	 *      3987: Internationalized Resource Identifiers (IRIs)</a>
	 */
	public static void assertValidIRI(String id) {
		try {
			@SuppressWarnings("unused")
			IRI iri = IRIFactory.iriImplementation().construct(id);
		} catch (IRIException e) {
			throw new AssertionError(ErrorMessage.format(
					ErrorMessageKeys.INVALID_IRI, id));
		}
	}

}
