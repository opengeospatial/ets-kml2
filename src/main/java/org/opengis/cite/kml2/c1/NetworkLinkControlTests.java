package org.opengis.cite.kml2.c1;

import javax.ws.rs.core.MediaType;

import org.opengis.cite.kml2.CommonFixture;
import org.opengis.cite.kml2.ETSAssert;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.XMLUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implements tests that apply to kml:NetworkLinkControl elements. Outside of an
 * update context the following constraints apply:
 * <ul>
 * <li>?</li>
 * </ul>
 * 
 * The relevant test cases from the abstract test suite are listed below:
 * <ul>
 * <li>ATC-110: Link referent (KML or KMZ resource)</li>
 * </ul>
 * 
 * @see "OGC 12-007r1: OGC KML 2.3, 9.15"
 * @see "OGC 14-068r1: OGC KML 2.3 - Abstract Test Suite, Conformance Level 1"
 */
public class NetworkLinkControlTests extends CommonFixture {

	public NetworkLinkControlTests() {
	}

	/**
	 * Finds kml:NetworkLinkControl elements in the KML document that do not
	 * appear in an update context. If none are found, all test methods defined
	 * in the class are skipped.
	 */
	@BeforeClass
	public void findNetworkLinkControlElements() {
		findTargetElements("NetworkLinkControl");
	}

	/**
	 * [Test] Verifies that a kml:NetworkLinkControl/kml:minRefreshPeriod
	 * element does not have a negative value (its datatype is xsd:double and
	 * the nominal default value is 0.0).
	 * 
	 * @param linkControl
	 *            A kml:NetworkLinkControl element.
	 */
	@Test(dataProvider = "targetElementsProvider", description = "ATC-120")
	public void refreshPeriod(Node linkControl) {
		Node refresh = Element.class.cast(linkControl)
				.getElementsByTagNameNS(KML2.NS_NAME, "minRefreshPeriod")
				.item(0);
		if (null == refresh) {
			return;
		}
		Assert.assertTrue(Double.parseDouble(refresh.getTextContent()) >= 0,
				ErrorMessage.format(ErrorMessageKeys.CONSTRAINT_VIOLATION,
						"kml:minRefreshPeriod >= 0",
						XMLUtils.buildXPointer(linkControl)));
	}

	/**
	 * [Test] Verifies that a kml:Update/kml:targetHref element contains an
	 * absolute URI that refers to a KML or KMZ resource.
	 * 
	 * @param linkControl
	 *            A kml:NetworkLinkControl element.
	 */
	@Test(dataProvider = "targetElementsProvider", description = "ATC-122")
	public void updateReferent(Node linkControl) {
		Node update = Element.class.cast(linkControl)
				.getElementsByTagNameNS(KML2.NS_NAME, "Update").item(0);
		if (null == update) {
			return;
		}
		Element targetUri = (Element) Element.class.cast(update)
				.getElementsByTagNameNS(KML2.NS_NAME, "targetHref").item(0);
		ETSAssert.assertReferentExists(targetUri,
				MediaType.valueOf(KML2.KML_MEDIA_TYPE),
				MediaType.valueOf(KML2.KMZ_MEDIA_TYPE));
	}
}
