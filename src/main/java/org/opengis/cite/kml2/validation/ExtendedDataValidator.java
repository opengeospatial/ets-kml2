package org.opengis.cite.kml2.validation;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathExpressionException;

import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.KMLUtils;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.validation.ErrorLocator;
import org.opengis.cite.validation.ErrorSeverity;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Checks constraints to apply to kml:ExtendedData elements. The relevant type
 * definition is shown below.
 * 
 * <pre>
 * {@literal
 * <xsd:complexType name="ExtendedDataType" final="#all">
 *   <xsd:all>
 * 	   <xsd:element ref="kml:Data" minOccurs="0" maxOccurs="unbounded"/>
 * 	   <xsd:element ref="kml:SchemaData" minOccurs="0" maxOccurs="unbounded"/>
 * 	   <xsd:any namespace="##other" processContents="lax" minOccurs="0" maxOccurs="unbounded"/>
 *   </xsd:all>
 *  <xsd:anyAttribute namespace="##other" processContents="lax"/>
 * </xsd:complexType>
 * }
 * </pre>
 * 
 * The applicable test cases are listed below.
 * <ul>
 * <li>ATC-128</li>
 * <li>ATC-233</li>
 * </ul>
 */
public class ExtendedDataValidator {

	ValidationErrorHandler errHandler;
	SchemaChecker schemaChecker;
	Document ownerDocument;

	/**
	 * Intended to facilitate unit testing only.
	 * 
	 * @param doc
	 *            The document containing the custom data.
	 */
	void setOwnerDocument(Document doc) {
		this.ownerDocument = doc;
	}

	/**
	 * Default constructor.
	 */
	public ExtendedDataValidator() {
		this.errHandler = new ValidationErrorHandler();
		this.schemaChecker = new SchemaChecker();
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
	 * Validates a kml:ExtendedData element.
	 * 
	 * @param node
	 *            A kml:ExtendedData element.
	 * @return true if the extended data elements are valid; false otherwise.
	 */
	public boolean isValid(Node node) {
		if (!node.getLocalName().equals("ExtendedData")) {
			throw new IllegalArgumentException("Not an ExtendedData element: "
					+ node.getLocalName());
		}
		errHandler.reset();
		this.ownerDocument = node.getOwnerDocument();
		Element extData = (Element) node;
		checkData(extData);
		checkSchemaData(extData);
		return !errHandler.errorsDetected();
	}

	/**
	 * Checks that a kml:Data element (an untyped name-value pair) satisfies all
	 * applicable constraints.
	 * <ol>
	 * <li>the value of the 'name' attribute is unique within the context of the
	 * parent kml:ExtendedData element</li>
	 * <li>if present, the uom attribute identifies a valid unit of measurement</li>
	 * </ol>
	 * 
	 * @param extData
	 *            A kml:ExtendedData element.
	 * 
	 * @see "OGC KML 2.3 – Abstract Test Suite, ATC-128: Data element has distinct name"
	 * @see "OGC KML 2.3 – Abstract Test Suite, ATC-233: Valid unit of measurement"
	 */
	void checkData(Element extData) {
		NodeList dataList = extData
				.getElementsByTagNameNS(KML2.NS_NAME, "Data");
		HashSet<String> nameSet = new HashSet<>(dataList.getLength());
		for (int i = 0; i < dataList.getLength(); i++) {
			Element data = (Element) dataList.item(i);
			String name = data.getAttribute("name");
			if (!nameSet.add(name)) {
				errHandler.addError(
						ErrorSeverity.ERROR,
						ErrorMessage.format(ErrorMessageKeys.DUPLICATE_DATA,
								name),
						new ErrorLocator(-1, -1, XMLUtils
								.buildXPointer(extData)));
			}
			schemaChecker.checkUnitOfMeasure(data);
		}
		this.errHandler.addErrors(schemaChecker.getErrors());
	}

	/**
	 * Checks that a kml:SchemaData element satisfies all applicable
	 * constraints:
	 * <ol>
	 * <li>the 'schemaUrl' attribute value is a URL (it may be an absolute URI)
	 * with a fragment component that refers to a kml:Schema element;</li>
	 * <li>all kml:SimpleData child elements have a 'name' attribute that
	 * matches the name of a declared kml:SimpleField element in the associated
	 * kml:Schema element (see ATC-126);</li>
	 * <li>the values of all kml:SimpleData child elements conform to their
	 * declared types.</li>
	 * <ol>
	 * 
	 * @param extData
	 *            A kml:ExtendedData element.
	 * 
	 * @see "OGC KML 2.3 - Abstract Test Suite, ATC-127: SchemaData content"
	 */
	void checkSchemaData(Element extData) {
		NodeList schemaDataList = extData.getElementsByTagNameNS(KML2.NS_NAME,
				"SchemaData");
		for (int i = 0; i < schemaDataList.getLength(); i++) {
			Element schemaData = (Element) schemaDataList.item(i);
			String schemaUrl = schemaData.getAttribute("schemaUrl");
			if (schemaUrl.isEmpty()) {
				errHandler.addError(
						ErrorSeverity.ERROR,
						ErrorMessage.format(
								ErrorMessageKeys.MISSING_INFOSET_ITEM,
								"@schemaUrl"),
						new ErrorLocator(-1, -1, XMLUtils
								.buildXPointer(schemaData)));
				continue;
			}
			URI schemaURI = URI.create(schemaUrl);
			XdmNode schema = null;
			try {
				schema = fetchSchema(schemaURI);
			} catch (SaxonApiException | RuntimeException e) {
				errHandler.addError(
						ErrorSeverity.ERROR,
						ErrorMessage.format(ErrorMessageKeys.NOT_FOUND,
								schemaURI),
						new ErrorLocator(-1, -1, XMLUtils
								.buildXPointer(schemaData)));
				continue;
			}
			Map<String, ItemType> schemaFields = KMLUtils
					.getDeclaredFields(schema);
			validateSchemaData(schemaData, schemaFields);
		}
	}

	/**
	 * Validates the content of a kml:SchemaData element against the associated
	 * custom schema. In particular, for each data element:
	 * <ol>
	 * <li>it has a 'name' attribute that matches the name of a declared field
	 * in the associated schema;</li>
	 * <li>all values conform to the declared type.</li>
	 * </ol>
	 * 
	 * @param schemaData
	 *            A kml:SchemaData element containing kml:SimpleData or
	 *            kml:SimpleArrayData elements.
	 * @param schemaFields
	 *            A Map containing information (name, type) about the fields
	 *            declared in the custom schema.
	 */
	void validateSchemaData(Element schemaData,
			Map<String, ItemType> schemaFields) {
		NodeList dataNodes = null;
		try {
			dataNodes = XMLUtils.evaluateXPath(schemaData,
					"kml:SimpleData | kml:SimpleArrayData", null);
		} catch (XPathExpressionException e) {
		}
		for (int i = 0; i < dataNodes.getLength(); i++) {
			Element data = (Element) dataNodes.item(i);
			String name = data.getAttribute("name");
			if (!schemaFields.containsKey(name)) {
				errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
						ErrorMessageKeys.CONSTRAINT_VIOLATION,
						"Name not found in custom schema: " + name),
						new ErrorLocator(-1, -1, XMLUtils.buildXPointer(data)));
			}
			String[] dataValues = getDataValues(data);
			for (String value : dataValues) {
				try {
					@SuppressWarnings("unused")
					XdmAtomicValue xdmValue = new XdmAtomicValue(value,
							schemaFields.get(name));
				} catch (SaxonApiException e) {
					errHandler.addError(ErrorSeverity.ERROR, ErrorMessage
							.format(ErrorMessageKeys.CONSTRAINT_VIOLATION,
									e.getMessage()), new ErrorLocator(-1, -1,
							XMLUtils.buildXPointer(data)));
				}
			}
		}
	}

	/**
	 * Gets the values supplied by the given schema data element.
	 * 
	 * @param data
	 *            A kml:SimpleData or kml:SimpleArrayData element.
	 * @return An array of strings containing the data value(s).
	 */
	String[] getDataValues(Element data) {
		String[] values = null;
		if (data.getLocalName().equals("SimpleData")) {
			values = new String[] { data.getTextContent().trim() };
		} else {
			NodeList valueList = data.getElementsByTagNameNS(KML2.NS_NAME,
					"value");
			values = new String[valueList.getLength()];
			for (int i = 0; i < valueList.getLength(); i++) {
				values[i] = valueList.item(i).getTextContent().trim();
			}
		}
		return values;
	}

	/**
	 * Fetches the kml:Schema element referenced by the given URI. A relative
	 * URI is expected to be a same-document reference.
	 * 
	 * @param schemaURI
	 *            A URI containing a fragment identifier that refers to a
	 *            kml:Schema element.
	 * @return An XdmNode representing a kml:Schema element.
	 * @throws SaxonApiException
	 *             If an error occurs while trying to retrieve the target
	 *             resource.
	 * @throws RuntimeException
	 *             If a matching kml:Schema element cannot be obtained.
	 */
	XdmNode fetchSchema(URI schemaURI) throws SaxonApiException,
			RuntimeException {
		String xpath;
		if (null == schemaURI.getScheme()) { // relative URI
			xpath = String.format("//kml:Schema[@id='%s']",
					schemaURI.getFragment());
		} else {
			// strip fragment identifier before dereferencing absolute URI
			int numSign = schemaURI.toString().indexOf('#');
			xpath = String.format("doc('%s')//kml:Schema[@id='%s']", schemaURI
					.toString().substring(0, numSign), schemaURI.getFragment());
		}
		Source src = new DOMSource(this.ownerDocument,
				this.ownerDocument.getBaseURI());
		XdmValue result = XMLUtils.evaluateXPath2(src, xpath, null);
		return (XdmNode) result.itemAt(0);
	}

}