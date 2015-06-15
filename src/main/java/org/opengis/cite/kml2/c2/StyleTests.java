package org.opengis.cite.kml2.c2;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.XMLUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Checks CL2 constraints that apply to a kml:Style element.
 * 
 * @see "OGC 14-068r1, OGC KML 2.3 - Abstract Test Suite: Conformance Level 2"
 */
public class StyleTests extends CommonFixture {

	/**
	 * Finds kml:Style elements in the KML document that do not appear in an
	 * update context. If none are found, all test methods defined in the class
	 * are skipped.
	 */
	@BeforeClass
	public void findStyleElements() {
		findTargetElements("Style");
	}

	/**
	 * [Test] Verifies that if a Style has a child PolyStyle element, it is not
	 * empty. In particular, the PolyStyle must contain at least one of the
	 * following KML elements: kml:color, kml:colorMode, kml:fill, or
	 * kml:outline.
	 */
	@Test(description = "ATC-201")
	public void polyStyleNotEmpty() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element style = (Element) targetElements.item(i);
			Node polyStyle = style.getElementsByTagNameNS(KML2.NS_NAME,
					"PolyStyle").item(0);
			if (null != polyStyle) {
				NodeList kmlItems = Element.class.cast(polyStyle)
						.getElementsByTagNameNS(KML2.NS_NAME, "*");
				Assert.assertTrue(kmlItems.getLength() > 0, ErrorMessage
						.format(ErrorMessageKeys.NO_CONTENT, "kml:PolyStyle",
								XMLUtils.buildXPointer(polyStyle)));
			}
		}
	}

}
