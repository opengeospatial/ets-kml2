package org.opengis.cite.kml2.c1;

import javax.xml.namespace.QName;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.XMLUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Implements ATC-101: Document element.
 * 
 * @see "OGC 14-068r1: OGC KML 2.3 - Abstract Test Suite, Conformance Level 1"
 */
public class ATC101 extends CommonFixture {

	/**
	 * [Test] Verifies that the document (root) element has [local name] =
	 * {@value org.opengis.cite.kml2.KML2#DOC_ELEMENT} and [namespace name] =
	 * {@value org.opengis.cite.kml2.KML2#NS_NAME}.
	 */
	@Test(description = "ATC-101")
	public void verifyDocumentElement() {
		QName qName = XMLUtils.getQName(this.kmlDoc.getDocumentElement());
		Assert.assertEquals(qName, new QName(KML2.NS_NAME, KML2.DOC_ELEMENT),
				"Document element has unexpected name.");
	}

}
