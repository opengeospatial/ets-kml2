package org.opengis.cite.kml2.c1;

import javax.ws.rs.core.MediaType;

import org.opengis.cite.kml2.CommonFeatureTests;
import org.opengis.cite.kml2.ErrorMessage;
import org.opengis.cite.kml2.ErrorMessageKeys;
import org.opengis.cite.kml2.KML2;
import org.opengis.cite.kml2.util.XMLUtils;
import org.opengis.cite.kml2.validation.LinkValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Implements tests that apply to kml:NetworkLink elements. Outside of an update
 * context the following constraints apply:
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
public class NetworkLinkTests extends CommonFeatureTests {

	private LinkValidator linkValidator;

	public NetworkLinkTests() {
		this.linkValidator = new LinkValidator(
				MediaType.valueOf(KML2.KML_MEDIA_TYPE),
				MediaType.valueOf(KML2.KMZ_MEDIA_TYPE));
	}

	/**
	 * Finds kml:NetworkLink elements in the KML document that do not appear in
	 * an update context. If none are found, all test methods defined in the
	 * class are skipped.
	 */
	@BeforeClass
	public void findNetworkLinkElements() {
		findTargetElements("NetworkLink");
	}

	/**
	 * [Test] Verifies that a kml:NetworkLink element has a valid reference to a
	 * KML (or KMZ) resource.
	 */
	@Test(description = "ATC-110")
	public void validLink() {
		for (int i = 0; i < targetElements.getLength(); i++) {
			Element networkLink = (Element) targetElements.item(i);
			NodeList link = networkLink.getElementsByTagNameNS(KML2.NS_NAME,
					"Link");
			if (link.getLength() == 0) {
				throw new AssertionError(ErrorMessage.format(
						ErrorMessageKeys.MISSING_INFOSET_ITEM, "kml:Link",
						XMLUtils.buildXPointer(networkLink)));
			}
			Assert.assertTrue(linkValidator.isValid(link.item(0)),
					linkValidator.getErrorMessages());
		}
	}

}
