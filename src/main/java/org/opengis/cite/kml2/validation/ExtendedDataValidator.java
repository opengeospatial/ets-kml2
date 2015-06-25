package org.opengis.cite.kml2.validation;

import org.opengis.cite.kml2.KML2;
import org.opengis.cite.validation.ValidationErrorHandler;
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
	 * constraints.
	 * 
	 * @param extData
	 *            A kml:ExtendedData element.
	 */
	void checkSchemaData(Element extData) {
		NodeList schemaDataList = extData.getElementsByTagNameNS(KML2.NS_NAME,
				"SchemaData");
		for (int i = 0; i < schemaDataList.getLength(); i++) {
			// Element schemaData = (Element) schemaDataList.item(i);
			// TODO
		}
	}

}