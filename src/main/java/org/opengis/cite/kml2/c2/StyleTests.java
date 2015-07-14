package org.opengis.cite.kml2.c2;

import java.net.URL;

import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ETSAssert;
import org.opengis.cite.kml2.util.XMLUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Checks CL2 constraints that apply to a style definition (kml:Style or
 * kml:StyleMap) element.
 * 
 * @see "OGC 14-068r1, OGC KML 2.3 - Abstract Test Suite: Conformance Level 2"
 */
public class StyleTests extends CommonFixture {

	/**
	 * Finds kml:Style and kml:StyleMap elements in the KML document that do not
	 * appear in an update context. If none are found, all test methods defined
	 * in the class are skipped.
	 */
	@BeforeClass
	public void findStyleSelectors() {
		findTargetElements("Style", "StyleMap");
	}

	/**
	 * [Test] Verifies that all style-related constraints that apply at CL2 are
	 * satisfied.
	 */
	@Test(description = "ATC-201,-217,-220,-222,-223,-224,-227")
	public void checkStyleConstraints() {
		URL schRef = this.getClass().getResource(
				"/org/opengis/cite/kml2/sch/kml-style.sch");
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element style = (Element) targetElements.item(i);
			ETSAssert.assertSchematronValid(schRef, new DOMSource(style));
		}
	}

	/**
	 * [Test] Verifies that replacement text exists for all entity references
	 * appearing in a kml:BalloonStyle/kml:text element. The sources of the
	 * replacement text include the ancestor feature being styled and any
	 * kml:Schema elements that are associated with it.
	 * 
	 * @see "OGC KML 2.3, 6.5: Entity Replacement"
	 * @see "OGC KML 2.3 - Abstract Test Suite, ATC-231"
	 */
	public void entitiesInBalloonStyle() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element style = (Element) targetElements.item(i);
			NodeList textNodes = null;
			try {
				textNodes = XMLUtils.evaluateXPath(style,
						"//kml:BalloonStyle/kml:text", null);
			} catch (XPathExpressionException e) {
			}
			for (int j = 0; j < textNodes.getLength(); j++) {
				// TODO: Find replacement text
				// If shared style, check all referring features.
			}
		}
	}

}
