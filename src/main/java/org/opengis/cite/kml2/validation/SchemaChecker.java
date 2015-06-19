package org.opengis.cite.kml2.validation;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

import org.opengis.cite.kml2.ETSAssert;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.validation.ErrorLocator;
import org.opengis.cite.validation.ErrorSeverity;
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
 */
public class SchemaChecker {

	private static final String UCUM_NS = "http://unitsofmeasure.org/ucum-essence";
	ValidationErrorHandler errHandler;
	URL uomCodeListRef;
	/** List of prefix symbols in UCUM ('c','k', 'M', ..). */
	List<String> uomPrefixes;

	/**
	 * Default constructor.
	 */
	public SchemaChecker() {
		this.errHandler = new ValidationErrorHandler();
		this.uomCodeListRef = SchemaChecker.class.getResource("ucum.xml");
		this.uomPrefixes = new ArrayList<String>();
		Source uomSource = new StreamSource(uomCodeListRef.toString());
		try {
			XdmValue result = XMLUtils.evaluateXPath2(uomSource,
					"//ucum:prefix/@Code",
					Collections.singletonMap(UCUM_NS, "ucum"));
			for (XdmItem item : result) {
				uomPrefixes.add(item.getStringValue());
			}
		} catch (SaxonApiException e) {
		}
	}

	List<String> getUomPrefixes() {
		return uomPrefixes;
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
		checkSimpleFields(schema);
		return !errHandler.errorsDetected();
	}

	/**
	 * Checks that a kml:SimpleField element satisfies all applicable
	 * constraints.
	 * 
	 * @param schema
	 *            A kml:SimpleField element.
	 * 
	 * @see "ATC-118: Icon element refers to image"
	 */
	void checkSimpleFields(Element schema) {
		NodeList simpleFields = schema.getElementsByTagNameNS(KML2.NS_NAME,
				"SimpleField");
		for (int i = 0; i < simpleFields.getLength(); i++) {
			Element simpleField = (Element) simpleFields.item(i);
			checkUnitOfMeasure(simpleField);
		}
	}

	/**
	 * Checks that a definition exists for a given unit of measure reference. If
	 * the reference is an absolute URI, the definition must exist but the
	 * format is irrelevant. Otherwise the reference must correspond to a code
	 * in the <em>Unified Code for Units of Measure</em> (UCUM). Prefix symbols
	 * may be used (e.g. 'km' for kilometre, 'ha' for hectare).
	 * 
	 * @param simpleField
	 *            A kml:SimpleField element.
	 * @see <a target="_blank"
	 *      href="http://unitsofmeasure.org/ucum.html">Unified Code for Units of
	 *      Measure</a>
	 */
	void checkUnitOfMeasure(Element simpleField) {
		String uom = simpleField.getAttribute("uom");
		if (uom.isEmpty()) {
			return;
		}
		URI uomRef = null;
		try {
			uomRef = URI.create(URLEncoder.encode(uom, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
		}
		if (uomRef.isAbsolute()) {
			ETSAssert
					.assertReferentExists(simpleField, MediaType.WILDCARD_TYPE);
			return;
		}
		String uomCode = uom;
		// remove prefix if present
		for (String prefix : uomPrefixes) {
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
										.buildXPointer(simpleField)));
			}
		} catch (SaxonApiException e) {
		}
	}
}