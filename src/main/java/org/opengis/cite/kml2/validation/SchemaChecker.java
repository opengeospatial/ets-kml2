package org.opengis.cite.kml2.validation;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.ItemTypeFactory;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmValue;

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
 * Checks constraints to apply to kml:Schema elements. The relevant type
 * definition is shown below (with extension points omitted).
 * 
 * <pre>
 * {@literal
 * <xsd:complexType name="SchemaType" final="#all">
 *   <xsd:all>
 * 	   <xsd:element ref="kml:SimpleField" minOccurs="0" maxOccurs="unbounded"/>
 * 	   <xsd:element ref="kml:SimpleArrayField" minOccurs="0" maxOccurs="unbounded"/>
 * 	   <xsd:element ref="kml:SchemaExtension" minOccurs="0" maxOccurs="unbounded"/>
 * 	 </xsd:all>
 *   <xsd:attribute name="name" type="string"/>
 * 	 <xsd:attribute name="id" type="ID"/>
 *   <xsd:anyAttribute namespace="##other" processContents="lax"/>
 * </xsd:complexType>
 * }
 * </pre>
 * 
 * The applicable test cases are identified below:
 * <ul>
 * <li>ATC-125: Schema identifier</li>
 * <li>ATC-126: SimpleField definition</li>
 * </ul>
 * 
 * @see "OGC KML 2.3, 9.11: kml:SimpleField"
 */
public class SchemaChecker {

	private static final String UCUM_NS = "http://unitsofmeasure.org/ucum-essence";
	ValidationErrorHandler errHandler;
	URL uomCodeListRef;
	/** List of common prefix symbols in UCUM ('c','k', 'M', ..). */
	List<String> commonPrefixes;

	/**
	 * Default constructor.
	 */
	public SchemaChecker() {
		this.errHandler = new ValidationErrorHandler();
		this.uomCodeListRef = SchemaChecker.class.getResource("ucum.xml");
		this.commonPrefixes = Arrays.asList(new String[] { "E", "P", "T", "G",
				"M", "k", "h", "da", "d", "c", "m", "u", "n", "p", "f", "a" });
	}

	List<String> getUomPrefixes() {
		return commonPrefixes;
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
	 * Returns all errors reported to the handler.
	 * 
	 * @return A list containing error descriptions (may be empty).
	 */
	public List<ValidationError> getErrors() {
		return errHandler.getErrors();
	}

	/**
	 * Validates a kml:Schema element.
	 * 
	 * @param node
	 *            A kml:Schema element.
	 * @return true if the schema is valid; false otherwise.
	 */
	public boolean isValid(Node node) {
		if (!node.getLocalName().equals("Schema")) {
			throw new IllegalArgumentException("Not a Schema element: "
					+ node.getLocalName());
		}
		errHandler.reset();
		Element schema = (Element) node;
		if (schema.getAttribute("id").isEmpty()) {
			errHandler.addError(ErrorSeverity.ERROR, ErrorMessage.format(
					ErrorMessageKeys.MISSING_INFOSET_ITEM, "@id"),
					new ErrorLocator(-1, -1, XMLUtils.buildXPointer(schema)));
		}
		checkSimpleFields(schema);
		checkSimpleArrayFields(schema);
		return !errHandler.errorsDetected();
	}

	/**
	 * Checks that a kml:SimpleField element satisfies all applicable
	 * constraints.
	 * 
	 * @param schema
	 *            A kml:Schema element.
	 * 
	 */
	void checkSimpleFields(Element schema) {
		NodeList simpleFields = schema.getElementsByTagNameNS(KML2.NS_NAME,
				"SimpleField");
		for (int i = 0; i < simpleFields.getLength(); i++) {
			Element simpleField = (Element) simpleFields.item(i);
			if (simpleField.getAttribute("name").isEmpty()) {
				errHandler
						.addError(ErrorSeverity.ERROR,
								ErrorMessage.format(
										ErrorMessageKeys.MISSING_INFOSET_ITEM,
										"@name"), new ErrorLocator(-1, -1,
										XMLUtils.buildXPointer(simpleField)));
			}
			checkDataType(simpleField);
			checkUnitOfMeasure(simpleField);
		}
	}

	/**
	 * Checks that a kml:SimpleArrayField element satisfies all applicable
	 * constraints.
	 * 
	 * @param schema
	 *            A kml:Schema element.
	 */
	void checkSimpleArrayFields(Element schema) {
		NodeList arrayFields = schema.getElementsByTagNameNS(KML2.NS_NAME,
				"SimpleArrayField");
		for (int i = 0; i < arrayFields.getLength(); i++) {
			Element arrayField = (Element) arrayFields.item(i);
			checkUnitOfMeasure(arrayField);
		}
	}

	/**
	 * Checks that the value of the 'type' attribute is a known (simple)
	 * datatype. Any of the primitive or derived datatypes defined in XML Schema
	 * (Part 2) are acceptable.
	 * 
	 * <p>
	 * <strong>Note:</strong> While a simple datatype that is derived from an
	 * XML Schema datatype is also allowed, such a user-defined datatype is not
	 * currently verified and will be reported as an error.
	 * </p>
	 * 
	 * @param simpleField
	 *            A kml:SimpleField element.
	 * 
	 * @see "OGC KML 2.3, 9.11.4.1: kml:SimpleField - type"
	 * @see <a href="http://www.w3.org/TR/xmlschema11-2/" target="_blank">W3C
	 *      XML Schema Definition Language (XSD) 1.1 Part 2: Datatypes</a>
	 */
	void checkDataType(Element simpleField) {
		String type = simpleField.getAttribute("type");
		if (type.isEmpty()) {
			errHandler.addError(
					ErrorSeverity.ERROR,
					ErrorMessage.format(ErrorMessageKeys.MISSING_INFOSET_ITEM,
							"@type"),
					new ErrorLocator(-1, -1, XMLUtils
							.buildXPointer(simpleField)));
			return;
		}
		QName typeName = new QName("http://www.w3.org/2001/XMLSchema", type);
		ItemTypeFactory typeFactory = new ItemTypeFactory(new Processor(false));
		try {
			@SuppressWarnings("unused")
			ItemType atomicType = typeFactory.getAtomicType(typeName);
		} catch (SaxonApiException e) {
			errHandler.addError(
					ErrorSeverity.ERROR,
					ErrorMessage.format(ErrorMessageKeys.INVALID_DATATYPE,
							e.getMessage()),
					new ErrorLocator(-1, -1, XMLUtils
							.buildXPointer(simpleField)));
		}
	}

	/**
	 * Checks that a definition exists for a given unit of measure reference. If
	 * the reference is an absolute URI, the definition must exist but the
	 * format is irrelevant. Otherwise the reference must correspond to a code
	 * in the <em>Unified Code for Units of Measure</em> (UCUM). Prefix symbols
	 * may be used (e.g. 'km' for kilometre, 'ha' for hectare).
	 * 
	 * @param schemaField
	 *            A kml:SimpleField or kml:SimpleArrayField element.
	 * @see <a target="_blank"
	 *      href="http://unitsofmeasure.org/ucum.html">Unified Code for Units of
	 *      Measure</a>
	 */
	public void checkUnitOfMeasure(Element schemaField) {
		String uom = schemaField.getAttribute("uom");
		if (uom.isEmpty()) {
			return;
		}
		try {
			URI uomRef = URI.create(URLEncoder.encode(uom, "UTF-8"));
			if (uomRef.isAbsolute()) {
				ETSAssert.assertReferentExists(uomRef, MediaType.WILDCARD_TYPE);
				return;
			}
		} catch (AssertionError | UnsupportedEncodingException e) {
			errHandler.addError(
					ErrorSeverity.ERROR,
					e.getMessage(),
					new ErrorLocator(-1, -1, XMLUtils
							.buildXPointer(schemaField)));
			return;
		}
		String uomCode = uom;
		// remove prefix if present
		for (String prefix : commonPrefixes) {
			if (uom.startsWith(prefix)) {
				uomCode = uom.replace(prefix, "");
				break;
			}
		}
		String xpath = String.format(
				"//(ucum:base-unit | ucum:unit)[@Code = '%s']", uomCode);
		Source uomSource = new StreamSource(uomCodeListRef.toString());
		try {
			XdmValue result = XMLUtils.evaluateXPath2(uomSource, xpath,
					Collections.singletonMap(UCUM_NS, "ucum"));
			if (result.size() == 0) {
				errHandler
						.addError(
								ErrorSeverity.ERROR,
								ErrorMessage.format(
										ErrorMessageKeys.UOM_NOT_DEFN, uom),
								new ErrorLocator(-1, -1, XMLUtils
										.buildXPointer(schemaField)));
			}
		} catch (SaxonApiException e) {
		}
	}
}