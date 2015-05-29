package org.opengis.cite.kml2.c2;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

/**
 * Checks CL2 constraints that apply to a KML document as a whole.
 * 
 * @see "OGC 14-068r1, OGC KML 2.3 - Abstract Test Suite: Conformance Level 2"
 */
public class KmlDocumentTests extends CommonFixture {

	/**
	 * [Test] Verifies that the document element (kml:kml) is not empty.
	 */
	@Test(description = "ATC-204")
	public void documentNotEmpty() {
		Element docElem = this.kmlDoc.getDocumentElement();
		Assert.assertTrue(docElem.hasChildNodes(),
				ErrorMessage.format(ErrorMessageKeys.NO_CONTENT, "kml:kml"));
	}

}
