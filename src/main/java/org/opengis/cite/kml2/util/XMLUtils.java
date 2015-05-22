package org.opengis.cite.kml2.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.s9api.DOMDestination;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.opengis.cite.kml2.KML2;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides various utility methods for accessing or manipulating XML
 * representations.
 */
public class XMLUtils {

	private static final Logger LOGR = Logger.getLogger(XMLUtils.class
			.getPackage().getName());
	private static final XMLInputFactory STAX_FACTORY = initXMLInputFactory();
	private static final XPathFactory XPATH_FACTORY = initXPathFactory();

	private static XPathFactory initXPathFactory() {
		XPathFactory factory = XPathFactory.newInstance();
		return factory;
	}

	private static XMLInputFactory initXMLInputFactory() {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
		return factory;
	}

	/**
	 * Writes the content of a DOM Node to a string. The XML declaration is
	 * omitted and the character encoding is set to "US-ASCII" (any character
	 * outside of this set is serialized as a numeric character reference).
	 *
	 * @param node
	 *            The DOM Node to be serialized.
	 * @return A String representing the content of the given node.
	 */
	public static String writeNodeToString(Node node) {
		if (null == node) {
			return "";
		}
		Writer writer = null;
		try {
			Transformer idTransformer = TransformerFactory.newInstance()
					.newTransformer();
			Properties outProps = new Properties();
			outProps.setProperty(OutputKeys.ENCODING, "US-ASCII");
			outProps.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			outProps.setProperty(OutputKeys.INDENT, "yes");
			idTransformer.setOutputProperties(outProps);
			writer = new StringWriter();
			idTransformer.transform(new DOMSource(node), new StreamResult(
					writer));
		} catch (TransformerException ex) {
			TestSuiteLogger.log(Level.WARNING, "Failed to serialize node "
					+ node.getNodeName(), ex);
		}
		return writer.toString();
	}

	/**
	 * Writes the content of a DOM Node to a byte stream. An XML declaration is
	 * always omitted.
	 * 
	 * @param node
	 *            The DOM Node to be serialized.
	 * @param outputStream
	 *            The destination OutputStream reference.
	 */
	public static void writeNode(Node node, OutputStream outputStream) {
		try {
			Transformer idTransformer = TransformerFactory.newInstance()
					.newTransformer();
			Properties outProps = new Properties();
			outProps.setProperty(OutputKeys.METHOD, "xml");
			outProps.setProperty(OutputKeys.ENCODING, "UTF-8");
			outProps.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			outProps.setProperty(OutputKeys.INDENT, "yes");
			idTransformer.setOutputProperties(outProps);
			idTransformer.transform(new DOMSource(node), new StreamResult(
					outputStream));
		} catch (TransformerException ex) {
			String nodeName = (node.getNodeType() == Node.DOCUMENT_NODE) ? Document.class
					.cast(node).getDocumentElement().getNodeName()
					: node.getNodeName();
			TestSuiteLogger.log(Level.WARNING, "Failed to serialize DOM node: "
					+ nodeName, ex);
		}
	}

	/**
	 * Evaluates an XPath 1.0 expression using the given context and returns the
	 * result as a node set.
	 * 
	 * @param context
	 *            The context node.
	 * @param expr
	 *            An XPath expression.
	 * @param namespaceBindings
	 *            A collection of namespace bindings for the XPath expression,
	 *            where each entry maps a namespace URI (key) to a prefix
	 *            (value). Standard bindings do not need to be declared (see
	 *            {@link NamespaceBindings#withStandardBindings()}.
	 * @return A NodeList containing nodes that satisfy the expression (it may
	 *         be empty).
	 * @throws XPathExpressionException
	 *             If the expression cannot be evaluated for any reason.
	 */
	public static NodeList evaluateXPath(Node context, String expr,
			Map<String, String> namespaceBindings)
			throws XPathExpressionException {
		Object result = evaluateXPath(context, expr, namespaceBindings,
				XPathConstants.NODESET);
		if (!NodeList.class.isInstance(result)) {
			throw new XPathExpressionException(
					"Expression does not evaluate to a NodeList: " + expr);
		}
		return (NodeList) result;
	}

	/**
	 * Evaluates an XPath expression using the given context and returns the
	 * result as the specified type.
	 * 
	 * <p>
	 * <strong>Note:</strong> The Saxon implementation supports XPath 2.0
	 * expressions when using the JAXP XPath APIs (the default implementation
	 * will throw an exception).
	 * </p>
	 * 
	 * @param context
	 *            The context node.
	 * @param expr
	 *            An XPath expression.
	 * @param namespaceBindings
	 *            A collection of namespace bindings for the XPath expression,
	 *            where each entry maps a namespace URI (key) to a prefix
	 *            (value). Standard bindings do not need to be declared (see
	 *            {@link NamespaceBindings#withStandardBindings()}.
	 * @param returnType
	 *            The desired return type (as declared in {@link XPathConstants}
	 *            ).
	 * @return The result converted to the desired returnType.
	 * @throws XPathExpressionException
	 *             If the expression cannot be evaluated for any reason.
	 */
	public static Object evaluateXPath(Node context, String expr,
			Map<String, String> namespaceBindings, QName returnType)
			throws XPathExpressionException {
		NamespaceBindings bindings = NamespaceBindings.withStandardBindings();
		bindings.addAllBindings(namespaceBindings);
		XPathFactory factory = XPATH_FACTORY;
		// WARNING: If context node is Saxon NodeOverNodeInfo, the factory must
		// use the same Configuration object to avoid IllegalArgumentException
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(bindings);
		Object result = xpath.evaluate(expr, context, returnType);
		return result;
	}

	/**
	 * Evaluates an XPath 2.0 expression using the Saxon s9api interfaces.
	 * 
	 * @param xmlSource
	 *            The XML Source.
	 * @param expr
	 *            The XPath expression to be evaluated.
	 * @param nsBindings
	 *            A collection of namespace bindings required to evaluate the
	 *            XPath expression, where each entry maps a namespace URI (key)
	 *            to a prefix (value); this may be {@code null} if not needed (a
	 *            binding for the "kml" prefix is always in effect).
	 * @return An XdmValue object representing a value in the XDM data model;
	 *         this is a sequence of zero or more items, where each item is
	 *         either an atomic value or a node.
	 * @throws SaxonApiException
	 *             If an error occurs while evaluating the expression; this
	 *             always wraps some other underlying exception.
	 */
	public static XdmValue evaluateXPath2(Source xmlSource, String expr,
			Map<String, String> nsBindings) throws SaxonApiException {
		Processor proc = new Processor(false);
		XPathCompiler compiler = proc.newXPathCompiler();
		compiler.declareNamespace("kml", KML2.NS_NAME);
		if (null != nsBindings) {
			for (String nsURI : nsBindings.keySet()) {
				compiler.declareNamespace(nsBindings.get(nsURI), nsURI);
			}
		}
		XPathSelector xpath = compiler.compile(expr).load();
		DocumentBuilder builder = proc.newDocumentBuilder();
		XdmNode node = null;
		if (DOMSource.class.isInstance(xmlSource)) {
			DOMSource domSource = (DOMSource) xmlSource;
			node = builder.wrap(domSource.getNode());
		} else {
			node = builder.build(xmlSource);
		}
		xpath.setContextItem(node);
		return xpath.evaluate();
	}

	/**
	 * Creates a new Element having the specified qualified name. The element
	 * must be {@link Document#adoptNode(Node) adopted} when inserted into
	 * another Document.
	 * 
	 * @param qName
	 *            A QName object.
	 * @return An Element node (with a Document owner but no parent).
	 */
	public static Element createElement(QName qName) {
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.newDocument();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		Element elem = doc.createElementNS(qName.getNamespaceURI(),
				qName.getLocalPart());
		return elem;
	}

	/**
	 * Returns a List of all descendant Element nodes having the specified
	 * [namespace name] property. The elements are listed in document order.
	 * 
	 * @param node
	 *            The node to search from.
	 * @param namespaceURI
	 *            An absolute URI denoting a namespace name.
	 * @return A List containing elements in the specified namespace; the list
	 *         is empty if there are no elements in the namespace.
	 */
	public static List<Element> getElementsByNamespaceURI(Node node,
			String namespaceURI) {
		List<Element> list = new ArrayList<Element>();
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE)
				continue;
			if (child.getNamespaceURI().equals(namespaceURI))
				list.add((Element) child);
		}
		return list;
	}

	/**
	 * Transforms the content of a DOM Node using a specified XSLT stylesheet.
	 * 
	 * @param xslt
	 *            A Source object representing a stylesheet (XSLT 1.0 or 2.0).
	 * @param source
	 *            A Node representing the XML source. If it is an Element node
	 *            it will be imported into a new DOM Document.
	 * @return A DOM Document containing the result of the transformation.
	 */
	public static Document transform(Source xslt, Node source) {
		Document sourceDoc = null;
		Document resultDoc = null;
		try {
			resultDoc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();
			if (source.getNodeType() == Node.DOCUMENT_NODE) {
				sourceDoc = (Document) source;
			} else {
				sourceDoc = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder().newDocument();
				sourceDoc.appendChild(sourceDoc.importNode(source, true));
			}
		} catch (ParserConfigurationException pce) {
			throw new RuntimeException(pce);
		}
		Processor processor = new Processor(false);
		XsltCompiler compiler = processor.newXsltCompiler();
		try {
			XsltExecutable exec = compiler.compile(xslt);
			XsltTransformer transformer = exec.load();
			transformer.setSource(new DOMSource(sourceDoc));
			transformer.setDestination(new DOMDestination(resultDoc));
			transformer.transform();
		} catch (SaxonApiException e) {
			throw new RuntimeException(e);
		}
		return resultDoc;
	}

	/**
	 * Expands character entity (&name;) and numeric references (&#xhhhh; or
	 * &dddd;) that occur within a given string value. It may be necessary to do
	 * this before processing an XPath expression.
	 * 
	 * @param value
	 *            A string representing text content.
	 * @return A string with all included references expanded.
	 */
	public static String expandReferencesInText(String value) {
		StringBuilder wrapper = new StringBuilder("<value>");
		wrapper.append(value).append("</value>");
		Reader reader = new StringReader(wrapper.toString());
		String str = null;
		try {
			XMLStreamReader xsr = STAX_FACTORY.createXMLStreamReader(reader);
			xsr.nextTag(); // document element
			str = xsr.getElementText();
		} catch (XMLStreamException xse) {
			LOGR.log(Level.WARNING, xse.getMessage(), xse);
		}
		return str;
	}

	/**
	 * Determines if the given stream contains XML content. The stream will be
	 * buffered and reset if necessary.
	 * 
	 * @param stream
	 *            The InputStream to read.
	 * @return true if the stream contains XML content; false otherwise.
	 */
	public static boolean isXML(InputStream stream) {
		if (!stream.markSupported()) {
			stream = new BufferedInputStream(stream, 1024);
		}
		stream.mark(1024);
		byte[] bytes = new byte[1024];
		try {
			try {
				stream.read(bytes);
			} finally {
				stream.reset();
			}
		} catch (IOException iox) {
			throw new RuntimeException("Failed to read or reset stream. "
					+ iox.getMessage());
		}
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader reader = factory
					.createXMLStreamReader(new ByteArrayInputStream(bytes));
			// If XML, now in START_DOCUMENT state; seek document element.
			reader.nextTag();
		} catch (XMLStreamException xse) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the qualified name of a DOM node.
	 * 
	 * @param node
	 *            A DOM node.
	 * @return A QName representing a qualified name.
	 */
	public static QName getQName(Node node) {
		String localName = (null == node.getLocalName()) ? "" : node
				.getLocalName();
		return new QName(node.getNamespaceURI(), localName);
	}

	/**
	 * Builds an XPointer that refers to the given node. If a shorthand pointer
	 * (using a schema-determined identifier) cannot be constructed, then a
	 * scheme-based pointer is derived that indicates the absolute location path
	 * of a node in a DOM document. The location is specified as a scheme-based
	 * XPointer having two parts:
	 * <ul>
	 * <li>an xmlns() part that declares a namespace binding context;</li>
	 * <li>an xpointer() part that includes an XPath expression using the
	 * abbreviated '//' syntax for selecting a descendant node.</li>
	 * </ul>
	 * 
	 * @param node
	 *            A node in a DOM document.
	 * @return A String containing either a shorthand or a scheme-based pointer
	 *         that refers to the node.
	 * 
	 * @see <a href="http://www.w3.org/TR/xptr-framework/"target="_blank">
	 *      XPointer Framework</a>
	 * @see <a href="http://www.w3.org/TR/xptr-xmlns/" target="_blank">XPointer
	 *      xmlns() Scheme</a>
	 * @see <a href="http://www.w3.org/TR/xptr-xpointer/"
	 *      target="_blank">XPointer xpointer() Scheme</a>
	 */
	public static String buildXPointer(Node node) {
		if (null == node) {
			return "";
		}
		StringBuilder xpointer = new StringBuilder();
		if (null != node.getAttributes()
				&& null != node.getAttributes().getNamedItem("id")) {
			String id = node.getAttributes().getNamedItem("id").getNodeValue();
			xpointer.append(node.getLocalName()).append("[@id='").append(id)
					.append("']");
			return xpointer.toString();
		}
		String nsURI = node.getNamespaceURI();
		String nsPrefix = node.getPrefix();
		if (null == nsPrefix)
			nsPrefix = "tns";
		// WARNING: Escaping rules are currently ignored.
		xpointer.append("xmlns(").append(nsPrefix).append("=").append(nsURI)
				.append(")");
		xpointer.append("xpointer((");
		switch (node.getNodeType()) {
		case Node.ELEMENT_NODE:
			// Find the element in the list of all similarly named descendants
			// of the document root.
			NodeList elementsByName = node.getOwnerDocument()
					.getElementsByTagNameNS(nsURI, node.getLocalName());
			for (int i = 0; i < elementsByName.getLength(); i++) {
				if (elementsByName.item(i).isSameNode(node)) {
					xpointer.append("//");
					xpointer.append(nsPrefix).append(':')
							.append(node.getLocalName()).append(")[")
							.append(i + 1).append("])");
					break;
				}
			}
			break;
		case Node.DOCUMENT_NODE:
			xpointer.append("/");
			break;
		case Node.ATTRIBUTE_NODE:
			Attr attrNode = (Attr) node;
			xpointer = new StringBuilder(
					buildXPointer(attrNode.getOwnerElement()));
			xpointer.insert(xpointer.lastIndexOf(")"),
					"/@" + attrNode.getName());
			break;
		default:
			xpointer.setLength(0);
			break;
		}
		return xpointer.toString();
	}
}
