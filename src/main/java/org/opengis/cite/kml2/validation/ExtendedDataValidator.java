package org.opengis.cite.kml2.validation;

import java.net.URI;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
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
	 * Checks that a kml:Data element satisfies all applicable constraints.
	 * 
	 * @param extData
	 *            A kml:ExtendedData element.
	 * 
	 */
	void checkData(Element extData) {
		NodeList dataList = extData
				.getElementsByTagNameNS(KML2.NS_NAME, "Data");
		for (int i = 0; i < dataList.getLength(); i++) {
			Element data = (Element) dataList.item(i);
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
			try {
				XdmNode schema = fetchSchema(schemaURI);
			} catch (SaxonApiException | RuntimeException e) {
				errHandler.addError(
						ErrorSeverity.ERROR,
						ErrorMessage.format(ErrorMessageKeys.NOT_FOUND,
								schemaURI),
						new ErrorLocator(-1, -1, XMLUtils
								.buildXPointer(schemaData)));
			}
		}
	}

	/**
	 * Fetches the kml:Schema element referenced by the given URI. A relative
	 * URI is expected to be a same-document reference.
	 * 
	 * @param schemaURI
	 *            A URI containing a fragment identifier that refers to a
	 *            kml:Schema element.
	 * @return
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