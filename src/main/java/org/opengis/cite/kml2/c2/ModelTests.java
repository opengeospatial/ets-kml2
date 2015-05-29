package org.opengis.cite.kml2.c2;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.XMLUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Checks CL2 constraints that apply to a kml:Model element.
 * 
 * @see "OGC 14-068r1, OGC KML 2.3 - Abstract Test Suite: Conformance Level 2"
 */
public class ModelTests extends CommonFixture {

	/**
	 * Finds kml:Model elements in the KML document that do not appear in an
	 * update context. If none are found, all test methods defined in the class
	 * are skipped.
	 */
	@BeforeClass
	public void findModelElements() {
		findTargetElements("Model");
	}

	/**
	 * [Test] Verifies that if a Model has a child kml:Scale element, it is not
	 * empty. That is, at least one scaling factor has a non-default value.
	 */
	@Test(description = "ATC-203")
	public void scaleNotEmpty() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element model = (Element) targetElements.item(i);
			NodeList items = model
					.getElementsByTagNameNS(KML2.NS_NAME, "Scale");
			if (items.getLength() > 0 && !items.item(0).hasChildNodes()) {
				throw new AssertionError(ErrorMessage.format(
						ErrorMessageKeys.NO_CONTENT, "kml:Scale",
						XMLUtils.buildXPointer(model)));
			}
		}
	}

}
